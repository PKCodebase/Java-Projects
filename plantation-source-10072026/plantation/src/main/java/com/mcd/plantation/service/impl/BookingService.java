package com.mcd.plantation.service.impl;

import com.mcd.plantation.dto.request.*;
import com.mcd.plantation.dto.response.*;
import com.mcd.plantation.entity.*;
import com.mcd.plantation.enums.BookingStatus;
import com.mcd.plantation.enums.OfficialRole;
import com.mcd.plantation.enums.SlotStatus;
import com.mcd.plantation.enums.UserTypeCode;
import com.mcd.plantation.exception.*;
import com.mcd.plantation.repository.*;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import com.mcd.plantation.util.Base64Utils;
import com.mcd.plantation.util.BookingRefGenerator;
import com.mcd.plantation.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

	private final BookingRepository bookingRepo;
	private final ParkSlotRepository slotRepo;
	private final SlotTreeInventoryRepository inventoryRepo;
	private final PaymentRepository paymentRepo;
	private final PaymentService paymentService;
//	private final EmailService emailService; // EMAIL DISABLED
	private final ParkOfficialAssignmentRepository assignmentRepo;
	private final OccasionRepository occasionRepo;
	private final PaymentTransactionRepository paymentTransactionRepository;
	private final SwagamService swagamService;

	@Value("${razorpay.gst-rate}")
	private BigDecimal gstRate;
	@Value("${razorpay.processing-fee}")
	private BigDecimal processingFee;

	// TEST MODE (PG_TEST_MODE=true): local fake gateway for demos/testing.
	// Off by default; must never be enabled in a real deployment.
	@Value("${pg.test-mode:false}")
	private boolean pgTestMode;
	@Value("${pg.frontend-base-url:http://localhost:4200}")
	private String pgFrontendBaseUrl;

	// ─────────────────────────────────────────────────────────
	// CREATE BOOKING (Step 1: reserve inventory + payment order)
	// ─────────────────────────────────────────────────────────

	/*
	 * @Transactional public BookingResponse createBooking1(CreateBookingRequest
	 * req, String citizenId) {
	 * 
	 * 
	 * Citizen citizen = citizenRepo.findById(citizenId) .orElseThrow(() ->
	 * ResourceNotFoundException.of("Citizen", citizenId));
	 * 
	 * 
	 * // 1. Acquire pessimistic write lock on inventory row SlotTreeInventory inv =
	 * inventoryRepo .findBySlotAndSpeciesWithLock(req.slotId(), req.speciesId())
	 * .orElseThrow(() -> new TreeNotInSlotException(
	 * "The selected tree is not available in this time slot."));
	 * 
	 * // 2. Validate inventory
	 * 
	 * 
	 * if (!inv.hasAvailability()) throw new OutOfStockException(
	 * inv.getSpecies().getCommonName() + " is sold out for this slot.");
	 * 
	 * //inv.reserve(req.quantity());
	 * 
	 * ParkSlot slot = inv.getSlot();
	 * 
	 * // 3. Validate slot capacity if (slot.isFull() || slot.getFreeSpots() <= 0)
	 * throw new SlotFullException("No spots remaining in the " +
	 * slot.getStartTime() + " slot.");
	 * 
	 * // 4. Reserve one unit inv.reserve(); inventoryRepo.save(inv);
	 * 
	 * // 5. Increment slot booked count slot.incrementBooked();
	 * slotRepo.save(slot);
	 * 
	 * // 6. Persist booking Occasion occasion = (req.occasionId() != null) ?
	 * occasionRepo.findById(req.occasionId()).orElse(null) : null; Booking booking
	 * = Booking.builder() .bookingRef(BookingRefGenerator.next(bookingRepo))
	 * .citizen(citizenId) .slot(slot) .inventory(inv) .occasion(occasion)
	 * .status(BookingStatus.PENDING_PAYMENT) .build(); bookingRepo.save(booking);
	 * 
	 * // 7. Create Razorpay payment order
	 * 
	 * BigDecimal price = inv.getSpecies().getPrice(); BigDecimal gst =
	 * price.multiply(gstRate).setScale(2, RoundingMode.HALF_UP); BigDecimal total =
	 * price.add(gst).add(processingFee);
	 * 
	 * 
	 * PaymentOrderResponse order =
	 * paymentService.createOrder(booking.getBookingId(), price, gst, total);
	 * 
	 * log.info("Booking created: {} for citizen {} — {} at {}",
	 * booking.getBookingRef(), citizenId, inv.getSpecies().getCommonName(),
	 * slot.getStartTime());
	 * 
	 * return toBookingResponse(booking, order); }
	 */

	@Transactional
	public BookingResponse createBooking(CreateBookingRequest req, String citizenId) {

		// Lock slot
		ParkSlot slot = slotRepo.findByIdForUpdate(req.slotId())
				.orElseThrow(() -> new ResourceNotFoundException("Slot not found"));

		if (slot.isFull()) {
			throw new SlotFullException("No spots remaining in slot " + slot.getStartTime());
		}

		// Load Occasion
		Occasion occasion = req.occasionId() == null ? null : occasionRepo.findById(req.occasionId()).orElse(null);

		// Fetch all inventories
		List<UUID> speciesIds = req.items().stream().map(BookingItemRequest::speciesId).toList();

		List<SlotTreeInventory> inventories = inventoryRepo.findAllBySlotAndSpeciesWithLock(req.slotId(), speciesIds);

		Map<UUID, SlotTreeInventory> inventoryMap = inventories.stream()
				.collect(Collectors.toMap(i -> i.getSpecies().getSpeciesId(), Function.identity()));

		BigDecimal subtotal = BigDecimal.ZERO;

		List<BookingItems> bookingItems = new ArrayList<>();

		for (BookingItemRequest itemReq : req.items()) {

			SlotTreeInventory inv = inventoryMap.get(itemReq.speciesId());

			if (inv == null) {
				throw new TreeNotInSlotException("Species not available in slot");
			}

			if (!inv.hasAvailability(itemReq.quantity())) {
				throw new OutOfStockException(
						inv.getSpecies().getCommonName() + " has only " + inv.getAvailableQty() + " plants available.");
			}

			// Reserve inventory
			inv.reserve(itemReq.quantity());

			BigDecimal unitPrice = inv.getSpecies().getPrice();

			BigDecimal lineAmount = unitPrice.multiply(BigDecimal.valueOf(itemReq.quantity()));

			subtotal = subtotal.add(lineAmount);

			BookingItems bookingItem = BookingItems.builder().inventory(inv).quantity(itemReq.quantity())
					.unitPrice(unitPrice).totalPrice(lineAmount).build();

			bookingItems.add(bookingItem);
		}

		// One citizen occupies one slot
		slot.incrementBooked();

		BigDecimal gst = subtotal.multiply(gstRate).setScale(2, RoundingMode.HALF_UP);

		BigDecimal total = subtotal.add(gst).add(processingFee);

		Booking booking = Booking.builder().bookingRef(BookingRefGenerator.next(bookingRepo)).citizen(citizenId)
				.slot(slot).occasion(occasion).status(BookingStatus.PENDING_PAYMENT).amountPaid(total).build();

		bookingItems.forEach(item -> item.setBooking(booking));

		booking.setItems(bookingItems);

		bookingRepo.save(booking);

		PaymentOrderResponse paymentOrder = paymentService.createOrder(booking.getBookingId(), subtotal, gst, total);

		log.info("Booking {} created for citizen {} containing {} species", booking.getBookingRef(), citizenId,
				bookingItems.size());

		return toBookingResponse(booking, paymentOrder);
	}

	// ─────────────────────────────────────────────────────────
	// CONFIRM PAYMENT
	// ─────────────────────────────────────────────────────────
	@Transactional
	public BookingResponse confirmPayment(String encryptedResponse, String loginUserSystemCode, String ipAddr) 
		 {
		
		
		String decoded = decodeResponse(encryptedResponse);
        if (decoded == null) {
            throw new PaymentVerificationException("Unable to decode payment response");
        }
        
        String txnNumber  = JsonUtil.getValueByKey(decoded, "transactionNumber");
        String statusCode = JsonUtil.getValueByKey(decoded, "txnResponseCode");
        String statusDesc = JsonUtil.getValueByKey(decoded, "txnResponseCodeDescription");
        
        PaymentTransaction paymentTransaction = paymentTransactionRepository.
        		findByPaymentTransactionNumber(txnNumber).stream().findFirst()
        		.orElseThrow(() -> new PaymentVerificationException("transactionNumber not found"));
        
        
		Booking booking = bookingRepo.findById(paymentTransaction.getBooking().getBookingId())
				.orElseThrow(() -> ResourceNotFoundException.of("Booking", txnNumber));

		if (booking.getStatus() != BookingStatus.PENDING_PAYMENT)
			throw new IllegalStateException("Booking is not awaiting payment: " + booking.getStatus());

		paymentService.verifyPayment(paymentTransaction, statusCode, loginUserSystemCode, ipAddr, decoded, statusDesc);
		
		if ("000".equals(statusCode)) {
			Payment payment = paymentRepo.findByBookingBookingId(booking.getBookingId())
					.orElseThrow(() -> ResourceNotFoundException.of("Payment", booking.getBookingId()));
			payment.setStatus(com.mcd.plantation.enums.PaymentStatus.SUCCESS);
			payment.setPaidAt(OffsetDateTime.now());
			paymentRepo.save(payment);

			booking.setStatus(BookingStatus.PAID);
			booking.setAmountPaid(payment.getTotalAmount());
			bookingRepo.save(booking);


			// EMAIL DISABLED
//			emailService.sendBookingConfirmation(booking);
			log.info("Payment confirmed for booking {}", booking.getBookingRef());
		}
		
		// Update payment record

		return toBookingResponse(booking, paymentOrderOf(booking));
	}

	// ─────────────────────────────────────────────────────────
	// SET PRESENCE MODE
	// ─────────────────────────────────────────────────────────
	@Transactional
	public BookingResponse setPresenceMode(UUID bookingId, SetPresenceModeRequest req, String citizenId) {
		Booking booking = bookingRepo.findByIdWithDetails(bookingId)
				.orElseThrow(() -> ResourceNotFoundException.of("Booking", bookingId));

		if (!booking.getCitizen().equals(citizenId))
			throw new org.springframework.security.access.AccessDeniedException("Not your booking");

		if (booking.getStatus() != BookingStatus.PAID && booking.getStatus() != BookingStatus.SCHEDULED)
			throw new IllegalStateException("Cannot set presence mode for booking in status: " + booking.getStatus());

		booking.setPresenceMode(req.presenceMode());
		booking.setStatus(BookingStatus.SCHEDULED);
		bookingRepo.save(booking);

		return toBookingResponse(booking, null);
	}

	// ─────────────────────────────────────────────────────────
	// CANCEL BOOKING
	// ─────────────────────────────────────────────────────────
	/*
	 * @Transactional public void cancelBooking1(UUID bookingId, String citizenId) {
	 * Booking booking = bookingRepo.findById(bookingId) .orElseThrow(() ->
	 * ResourceNotFoundException.of("Booking", bookingId));
	 * 
	 * if (!booking.getCitizen().equals(citizenId)) throw new
	 * org.springframework.security.access.AccessDeniedException("Not your booking"
	 * );
	 * 
	 * if (!booking.isCancellable()) throw new
	 * CancellationNotAllowedException("Cannot cancel booking in status: " +
	 * booking.getStatus());
	 * 
	 * // Release slot-level inventory SlotTreeInventory inv =
	 * booking.getInventory(); inv.release(); inventoryRepo.save(inv);
	 * 
	 * // Release slot spot ParkSlot slot = booking.getSlot();
	 * slot.decrementBooked(); slotRepo.save(slot);
	 * 
	 * booking.setStatus(BookingStatus.CANCELLED); bookingRepo.save(booking);
	 * 
	 * // Initiate refund if paid if (booking.getStatus() == BookingStatus.PAID ||
	 * booking.getAmountPaid() != null) { paymentService.initiateRefund(bookingId);
	 * }
	 * 
	 * log.info("Booking cancelled: {}", booking.getBookingRef()); }
	 */
	
	
	@Transactional
	public void cancelBooking(UUID bookingId, String citizenId) {

	    Booking booking = bookingRepo.findByIdForUpdate(bookingId)
	            .orElseThrow(() ->
	                    ResourceNotFoundException.of("Booking", bookingId));

	    if (!booking.getCitizen().equals(citizenId)) {
	        throw new AccessDeniedException("Not your booking");
	    }

	    if (!booking.isCancellable()) {
	        throw new CancellationNotAllowedException(
	                "Cannot cancel booking in status: " + booking.getStatus());
	    }

	    // Release inventory for each booked species
	    for (BookingItems item : booking.getItems()) {

	        SlotTreeInventory inventory = item.getInventory();

	        inventory.release(item.getQuantity());
	    }

	    // Release one slot occupancy
	    ParkSlot slot = booking.getSlot();
	    slot.decrementBooked();

	    // Update booking status
	    booking.setStatus(BookingStatus.CANCELLED);

	    /*
	     * No explicit save() calls required because all entities are managed
	     * inside the transaction.
	     */

	    // Refund only if payment was completed
	    if (booking.getStatus() == BookingStatus.PAID
	            || booking.getAmountPaid() != null) {

	    	// ???????
	        //paymentService.initiateRefund(bookingId);
	    }

	    log.info("Booking {} cancelled by citizen {}",
	            booking.getBookingRef(),
	            citizenId);
	}

	// ─────────────────────────────────────────────────────────
	// QUERIES
	// ─────────────────────────────────────────────────────────
	@Transactional(readOnly = true)
	public List<BookingResponse> getMyBookings(String citizenId) {
		return bookingRepo.findByCitizenWithDetails(citizenId).stream().map(b -> toBookingResponse(b, null))
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<BookingResponse> getBookingsByStatus(com.mcd.plantation.enums.BookingStatus status) {
		return bookingRepo.findByStatus(status).stream().map(b -> toBookingResponse(b, null))
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<BookingResponse> getScheduledBookingsForOfficial(String official, String officalRoleCode, 
			LocalDate fromDate, LocalDate toDate) {
		// Include WAITING so physical-presence bookings remain visible after "Mark
		// Arrived"
		List<BookingStatus> active = List.of(BookingStatus.PAID, BookingStatus.SCHEDULED, BookingStatus.WAITING);
		List<Booking> bookings;
		if (officalRoleCode.equals(OfficialRole.R_HORTIC_ADM.name())) {
			bookings = bookingRepo.findByStatusIn(active, fromDate, toDate);
		} else {
			List<UUID> parkIds = assignmentRepo.findParkIdsByOfficialId(official);
			bookings = parkIds.isEmpty() ? List.of() : bookingRepo.findByStatusInAndParkIds(active, parkIds,
					fromDate, toDate);
		}
		return bookings.stream().map(b -> toBookingResponse(b, null)).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<BookingResponse> getTodaysScheduledBookingsForOfficial(String official, String officalRoleCode) {
		// Include WAITING so physical-presence bookings remain visible after "Mark
		// Arrived"
		LocalDate today = LocalDate.now();
		List<BookingStatus> active = List.of(BookingStatus.PAID, BookingStatus.SCHEDULED, BookingStatus.WAITING);
		List<Booking> bookings;
		if (officalRoleCode.equals(OfficialRole.R_HORTIC_ADM.name())) {
			bookings = bookingRepo.findByStatusInAndSlotDate(active, today);
		} else {
			List<UUID> parkIds = assignmentRepo.findParkIdsByOfficialId(official);
			bookings = parkIds.isEmpty() ? List.of()
					: bookingRepo.findByStatusInAndParkIdsAndSlotDate(active, today, parkIds);
		}
		return bookings.stream().map(b -> toBookingResponse(b, null)).collect(Collectors.toList());
	}

	@Transactional
	public BookingResponse markCitizenArrived(UUID bookingId, String loginOfficerSystemCode, String officerRoleCode) {
		Booking booking = bookingRepo.findByIdWithDetails(bookingId)
				.orElseThrow(() -> ResourceNotFoundException.of("Booking", bookingId));

		if (booking.getStatus() != BookingStatus.PAID && booking.getStatus() != BookingStatus.SCHEDULED)
			throw new IllegalStateException("Booking is not in a schedulable state: " + booking.getStatus());

		if (!officerRoleCode.equals(OfficialRole.R_HORTIC_ADM.name())) {
			List<UUID> parkIds = assignmentRepo.findParkIdsByOfficialId(loginOfficerSystemCode);
			if (!parkIds.contains(booking.getSlot().getPark().getParkId()))
				throw new org.springframework.security.access.AccessDeniedException("Not assigned to this park");
		}

		booking.setStatus(BookingStatus.WAITING);
		bookingRepo.save(booking);
		return toBookingResponse(booking, null);
	}

	/*
	 * @Transactional public BookingResponse rescheduleBooking1(UUID bookingId, UUID
	 * newSlotId, String loginOfficerSystemCode, String loginOfficerRoleCode) {
	 * Booking booking = bookingRepo.findById(bookingId) .orElseThrow(() ->
	 * ResourceNotFoundException.of("Booking", bookingId));
	 * 
	 * if (booking.getStatus() != BookingStatus.PAID && booking.getStatus() !=
	 * BookingStatus.SCHEDULED) throw new
	 * IllegalStateException("Cannot reschedule booking in status: " +
	 * booking.getStatus());
	 * 
	 * if (!loginOfficerRoleCode.equals(OfficialRole.R_HORTIC_ADM.name())) {
	 * List<UUID> parkIds =
	 * assignmentRepo.findParkIdsByOfficialId(loginOfficerSystemCode); if
	 * (!parkIds.contains(booking.getSlot().getPark().getParkId())) throw new
	 * org.springframework.security.access.
	 * AccessDeniedException("Not assigned to this park"); }
	 * 
	 * if (newSlotId.equals(booking.getSlot().getSlotId())) return
	 * toBookingResponse(booking, null);
	 * 
	 * UUID speciesId = booking.getInventory().getSpecies().getSpeciesId();
	 * SlotTreeInventory newInv =
	 * inventoryRepo.findBySlotAndSpeciesWithLock(newSlotId, speciesId)
	 * .orElseThrow(() -> new
	 * TreeNotInSlotException("Species not available in the target slot"));
	 * 
	 * if (!newInv.hasAvailability()) throw new
	 * OutOfStockException(newInv.getSpecies().getCommonName() +
	 * " is out of stock in the target slot");
	 * 
	 * ParkSlot newSlot = newInv.getSlot(); if (newSlot.getStatus() ==
	 * com.mcd.plantation.enums.SlotStatus.CLOSED) throw new
	 * SlotFullException("Target slot is closed");
	 * 
	 * SlotTreeInventory oldInv = booking.getInventory(); oldInv.release();
	 * inventoryRepo.save(oldInv); ParkSlot oldSlot = booking.getSlot();
	 * oldSlot.decrementBooked(); slotRepo.save(oldSlot);
	 * 
	 * newInv.reserve(); inventoryRepo.save(newInv); newSlot.incrementBooked();
	 * slotRepo.save(newSlot);
	 * 
	 * booking.setSlot(newSlot); booking.setInventory(newInv);
	 * bookingRepo.save(booking);
	 * 
	 * log.info("Booking {} rescheduled to slot {} by official {}",
	 * booking.getBookingRef(), newSlotId, loginOfficerSystemCode); return
	 * toBookingResponse(booking, null); }
	 */
	
	
	@Transactional
	public BookingResponse rescheduleBooking(
	        UUID bookingId,
	        UUID newSlotId,
	        String loginOfficerSystemCode,
	        String loginOfficerRoleCode) {

	    Booking booking = bookingRepo.findByIdForUpdate(bookingId)
	            .orElseThrow(() ->
	                    ResourceNotFoundException.of("Booking", bookingId));

	    if (booking.getStatus() != BookingStatus.PAID
	            && booking.getStatus() != BookingStatus.SCHEDULED) {
	        throw new IllegalStateException(
	                "Cannot reschedule booking in status: "
	                        + booking.getStatus());
	    }

	    // Park authorization
	    if (!OfficialRole.R_HORTIC_ADM.name().equals(loginOfficerRoleCode)) {

	        List<UUID> parkIds =
	                assignmentRepo.findParkIdsByOfficialId(loginOfficerSystemCode);

	        if (!parkIds.contains(booking.getSlot().getPark().getParkId())) {
	            throw new AccessDeniedException("Not assigned to this park");
	        }
	    }

	    if (booking.getSlot().getSlotId().equals(newSlotId)) {
	        return toBookingResponse(booking, null);
	    }

	    // Lock new slot
	    ParkSlot newSlot = slotRepo.findByIdForUpdate(newSlotId)
	            .orElseThrow(() ->
	                    ResourceNotFoundException.of("Slot", newSlotId));

	    if (newSlot.getStatus() == SlotStatus.CLOSED) {
	        throw new SlotFullException("Target slot is closed");
	    }

	    if (newSlot.isFull()) {
	        throw new SlotFullException("Target slot is full");
	    }

	    // Collect all booked species
	    List<UUID> speciesIds = booking.getItems()
	            .stream()
	            .map(item -> item.getInventory()
	                    .getSpecies()
	                    .getSpeciesId())
	            .toList();

	    // Lock inventories in new slot
	    List<SlotTreeInventory> newInventories =
	            inventoryRepo.findAllBySlotAndSpeciesWithLock(
	                    newSlotId,
	                    speciesIds);

	    Map<UUID, SlotTreeInventory> inventoryMap =
	            newInventories.stream()
	                    .collect(Collectors.toMap(
	                            i -> i.getSpecies().getSpeciesId(),
	                            Function.identity()));

	    // Validate availability
	    for (BookingItems item : booking.getItems()) {

	        UUID speciesId =
	                item.getInventory().getSpecies().getSpeciesId();

	        SlotTreeInventory newInventory =
	                inventoryMap.get(speciesId);

	        if (newInventory == null) {
	            throw new TreeNotInSlotException(
	                    item.getInventory()
	                            .getSpecies()
	                            .getCommonName()
	                            + " not available in target slot");
	        }

	        if (!newInventory.hasAvailability(item.getQuantity())) {
	            throw new OutOfStockException(
	                    item.getInventory()
	                            .getSpecies()
	                            .getCommonName()
	                            + " has insufficient stock in target slot");
	        }
	    }

	    // Release old inventory
	    for (BookingItems item : booking.getItems()) {

	        item.getInventory().release(item.getQuantity());
	    }

	    // Reserve new inventory and update booking items
	    for (BookingItems item : booking.getItems()) {

	        UUID speciesId =
	                item.getInventory().getSpecies().getSpeciesId();

	        SlotTreeInventory newInventory =
	                inventoryMap.get(speciesId);

	        newInventory.reserve(item.getQuantity());

	        item.setInventory(newInventory);
	    }

	    // Update slot occupancy
	    ParkSlot oldSlot = booking.getSlot();

	    oldSlot.decrementBooked();
	    newSlot.incrementBooked();

	    booking.setSlot(newSlot);

	    log.info("Booking {} rescheduled from slot {} to slot {}",
	            booking.getBookingRef(),
	            oldSlot.getSlotId(),
	            newSlotId);

	    return toBookingResponse(booking, null);
	}
	

	public BookingResponse getBooking(UUID bookingId, String citizenId) {
		Booking b = bookingRepo.findByIdWithDetails(bookingId)
				.orElseThrow(() -> ResourceNotFoundException.of("Booking", bookingId));
		if (!b.getCitizen().equals(citizenId))
			throw new org.springframework.security.access.AccessDeniedException("Not your booking");
		return toBookingResponse(b, paymentOrderOf(b));
	}

	/**
	 * Money breakdown of a booking that already has a payment row, so a citizen
	 * who comes back later (detail page / after the gateway return) sees the
	 * same GST + processing-fee split the create response carried. The stored
	 * figures are echoed — no rate is recomputed here.
	 */
	@Nullable
	private PaymentOrderResponse paymentOrderOf(Booking booking) {
		return paymentRepo.findByBookingBookingId(booking.getBookingId())
				.map(p -> new PaymentOrderResponse(
						null, "INR", p.getAmount(), p.getGstAmount(), p.getFeeAmount(),
						p.getTotalAmount(), null, null))
				.orElse(null);
	}

	// ─────────────────────────────────────────────────────────
	// MAPPER
	// ─────────────────────────────────────────────────────────
	
	/*
	 * private BookingResponse toBookingResponse1(Booking b, @Nullable
	 * PaymentOrderResponse order) { System.out.println("inv " +
	 * b.getInventory().getInventoryId()); SlotTreeInventory inv = b.getInventory();
	 * TreeSpecies sp = inv.getSpecies(); ParkSlot slot = inv.getSlot(); Occasion
	 * occ = b.getOccasion();
	 * 
	 * 
	 * return new BookingResponse(b.getBookingId(), b.getBookingRef(),
	 * "Pushpendra Singh", "javapst@gmail.com", "Phone", "5128", slot.getSlotId(),
	 * slot.getPark().getParkId(), slot.getPark().getName(), sp.getSpeciesId(),
	 * slot.getSlotDate(), slot.getStartTime(), sp.getCommonName(),
	 * sp.getEmojiCode(), b.getStatus(), b.getPresenceMode(), b.getAmountPaid(),
	 * b.getBookedAt(), order, occ != null ? occ.getOccasionId() : null, occ != null
	 * ? occ.getName() : null, occ != null ? occ.getEmojiCode() : null);
	 * 
	 * return new BookingResponse( b.getBookingId(), b.getBookingRef(),
	 * b.getCitizen().getFullName(), b.getCitizen().getEmail(),
	 * b.getCitizen().getPhone(), b.getCitizen().getAadhaarLast4(),
	 * slot.getSlotId(), slot.getPark().getParkId(), slot.getPark().getName(),
	 * sp.getSpeciesId(), slot.getSlotDate(), slot.getStartTime(),
	 * sp.getCommonName(), sp.getEmojiCode(), b.getStatus(), b.getPresenceMode(),
	 * b.getAmountPaid(), b.getBookedAt(), order, occ != null ? occ.getOccasionId()
	 * : null, occ != null ? occ.getName() : null, occ != null ? occ.getEmojiCode()
	 * : null );
	 * 
	 * }
	 */
	
	
	
	private BookingResponse toBookingResponse(
	        Booking booking,
	        @Nullable PaymentOrderResponse order) {

		UserSummaryResponse citizen = swagamService.fetchUserSummary(booking.getCitizen(), 
				UserTypeCode.UT_CTZ.name());
		
	    ParkSlot slot = booking.getSlot();

	    Occasion occasion = booking.getOccasion();

	    List<BookingItemResponse> items =
	            booking.getItems()
	                    .stream()
	                    .map(item -> {

	                        TreeSpecies species =
	                                item.getInventory().getSpecies();

	                        return new BookingItemResponse(

	                                item.getInventory().getInventoryId(),

	                                species.getSpeciesId(),

	                                species.getCommonName(),

	                                species.getEmojiCode(),

	                                item.getQuantity(),

	                                item.getUnitPrice(),

	                                item.getTotalPrice()

	                        );

	                    })
	                    .toList();

	    return new BookingResponse(

	            booking.getBookingId(),

	            booking.getBookingRef(),

	            citizen.fullName(),

	            citizen.email(),

	            citizen.phone(),

	            "",

	            slot.getSlotId(),

	            slot.getPark().getParkId(),

	            slot.getPark().getName(),

	            slot.getSlotDate(),

	            slot.getStartTime(),

	            items,

	            booking.getStatus(),

	            booking.getPresenceMode(),

	            booking.getAmountPaid(),

	            booking.getBookedAt(),

	            order,

	            occasion != null ? occasion.getOccasionId() : null,

	            occasion != null ? occasion.getName() : null,

	            occasion != null ? occasion.getEmojiCode() : null

	    );
	}

	// ─────────────────────────────────────────────────────────
	// INIT PAYMENT — hand the citizen the gateway payment URL
	// ─────────────────────────────────────────────────────────
	/**
	 * The gateway session is created once, while the booking is created
	 * (PaymentService#createOrder). This call does not touch the gateway again —
	 * it returns the redirect URL stored with that session, so a citizen who
	 * closed the tab can pay later from "My Bookings" without creating a second
	 * transaction.
	 */
	@Transactional
	public InitPaymentResponse initializePayment(UUID bookingId, String loginUserSystemCode) {
		Booking booking = bookingRepo.findByIdWithDetails(bookingId)
				.orElseThrow(() -> ResourceNotFoundException.of("Booking", bookingId));

		if (loginUserSystemCode == null || !booking.getCitizen().equals(loginUserSystemCode))
			throw new AccessDeniedException("Not your booking");

		if (booking.getStatus() != BookingStatus.PENDING_PAYMENT)
			throw new IllegalStateException("Booking is not awaiting payment: " + booking.getStatus());

		// TEST MODE (PG_TEST_MODE=true): never hand out the real gateway URL —
		// send the citizen to the local fake-gateway page instead. Nothing
		// downstream changes: the result still flows through the REAL
		// /bookings/payment/verify endpoint, so the production path is covered.
		if (pgTestMode) {
			String testUrl = pgFrontendBaseUrl + "/citizen/payment/test?bookingId="
					+ booking.getBookingId() + "&ref=" + booking.getBookingRef();
			log.info("TEST payment mode: booking {} sent to the local fake gateway", booking.getBookingRef());
			return new InitPaymentResponse(testUrl,
					"TEST MODE (PG_TEST_MODE=true) — local fake gateway, no real payment is made");
		}

		String url = paymentTransactionRepository.findByBookingBookingIdOrderByCreatedDateDesc(bookingId)
				.stream()
				.map(PaymentTransaction::getPgResponseJson)
				.filter(json -> json != null && !json.isBlank())
				.map(json -> JsonUtil.getValueByKey(json, "redirectUrl"))
				.filter(found -> found != null && !found.isBlank())
				.findFirst()
				.orElse(null);

		if (url != null) {
			log.info("Payment URL handed out for booking {}", booking.getBookingRef());
			return new InitPaymentResponse(url, "Redirecting to the MCD payment gateway");
		}

		// No URL in the stored reply — say so plainly instead of pretending.
		log.warn("No payment URL stored for booking {}", booking.getBookingRef());
		return new InitPaymentResponse(null,
				"The payment gateway did not return a payment URL for booking " + booking.getBookingRef()
						+ ". The booking is still awaiting payment — try again once the gateway is reachable.");
	}
	
	
	// ─────────────────────────────────────────────────────────
	// TEST MODE — mint a fake gateway response (PG_TEST_MODE=true only)
	// ─────────────────────────────────────────────────────────
	/**
	 * Builds the exact Base64(JSON) shape the MCD gateway returns
	 * (transactionNumber / txnResponseCode / txnResponseCodeDescription) so a
	 * test payment runs through the REAL /bookings/payment/verify endpoint —
	 * success/failure is whatever the server then decides, never a fake UI.
	 *
	 * Gated: with pg.test-mode off this answers NotFound, so the endpoint is
	 * not a usable API in a normal environment.
	 *
	 * @param result "success" → txnResponseCode 000 (booking becomes PAID),
	 *               anything else → a failure code (booking stays PENDING_PAYMENT)
	 */
	@Transactional(readOnly = true)
	public Map<String, String> buildTestPaymentPayload(UUID bookingId, String loginUserSystemCode, String result) {
		if (!pgTestMode)
			throw ResourceNotFoundException.of("Booking", bookingId);

		Booking booking = bookingRepo.findByIdWithDetails(bookingId)
				.orElseThrow(() -> ResourceNotFoundException.of("Booking", bookingId));

		if (loginUserSystemCode == null || !booking.getCitizen().equals(loginUserSystemCode))
			throw new AccessDeniedException("Not your booking");

		if (booking.getStatus() != BookingStatus.PENDING_PAYMENT)
			throw new IllegalStateException("Booking is not awaiting payment: " + booking.getStatus());

		String txnNumber = paymentTransactionRepository.findByBookingBookingIdOrderByCreatedDateDesc(bookingId)
				.stream()
				.map(PaymentTransaction::getPaymentTransactionNumber)
				.filter(found -> found != null && !found.isBlank())
				.findFirst()
				.orElseThrow(() -> new PaymentVerificationException(
						"No payment transaction exists for booking " + booking.getBookingRef()));

		boolean success = !"failure".equalsIgnoreCase(result) && !"fail".equalsIgnoreCase(result);
		String json = "{\"transactionNumber\":\"" + txnNumber + "\""
				+ ",\"txnResponseCode\":\"" + (success ? "000" : "102") + "\""
				+ ",\"txnResponseCodeDescription\":\"" + (success ? "SUCCESS (TEST MODE)" : "FAILED (TEST MODE)")
				+ "\"}";
		String payload = Base64Utils.encodeToString(json);

		log.info("TEST payment payload ({}) minted for booking {}", success ? "success" : "failure",
				booking.getBookingRef());
		return Map.of("encryptedResponse", payload);
	}

	private String decodeResponse(String encoded) {
        try {
            String decoded = Base64Utils.decodeToString(encoded);
            if (!JsonUtil.isValidJson(decoded)) {
                log.error("Decoded payment response is not valid JSON");
                return null;
            }
            return decoded;
        } catch (Exception e) {
            log.error("Failed to decode payment response", e);
            return null;
        }
    }
	


}
