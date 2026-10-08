package com.mcd.plantation.service.impl;

import com.mcd.plantation.dto.request.RecordPlantationRequest;
import com.mcd.plantation.dto.response.CompletedPlantationItemResponse;
import com.mcd.plantation.dto.response.CompletedPlantationResponse;
import com.mcd.plantation.dto.response.PlantationRecordResponse;
import com.mcd.plantation.dto.response.PlantationRecordTreeResponse;
import com.mcd.plantation.entity.*;
import com.mcd.plantation.enums.BookingStatus;
import com.mcd.plantation.enums.OfficialRole;
import com.mcd.plantation.enums.UserTypeCode;
import com.mcd.plantation.exception.DuplicateResourceException;
import com.mcd.plantation.exception.ResourceNotFoundException;
import com.mcd.plantation.repository.BookingRepository;
import com.mcd.plantation.repository.CertificateRepository;
import com.mcd.plantation.repository.ParkOfficialAssignmentRepository;
import com.mcd.plantation.repository.PlantationRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor @Slf4j
public class PlantationRecordService {

    private final PlantationRecordRepository recordRepo;
    private final BookingRepository          bookingRepo;
    private final CertificateService         certService;
    private final CertificateRepository      certRepo;
    private final ParkOfficialAssignmentRepository assignmentRepo;
    private final FileStorageService         fileStorage;
    private final SwagamService              swagamService;

    @Transactional
    public PlantationRecordResponse recordPlantation(UUID bookingId, RecordPlantationRequest req,
                                                     MultipartFile photo, String officialId) {
        Booking booking = bookingRepo.findById(bookingId)
            .orElseThrow(() -> ResourceNotFoundException.of("Booking", bookingId));

		/*
		 * McdOfficial official = officialRepo.findById(officialId) .orElseThrow(() ->
		 * ResourceNotFoundException.of("Official", officialId));
		 */

        if (recordRepo.findByBookingBookingId(bookingId).isPresent())
            throw new DuplicateResourceException("Plantation already recorded for this booking.");

        String photoUrl = fileStorage.store(photo, "plantation");

        PlantationRecord record = PlantationRecord.builder()
            .booking(booking).official(officialId)
            .treeTagId(req.treeTagId())
            .photoBucket("local")
            .photoKey("plantation/" + photoUrl.substring(photoUrl.lastIndexOf('/') + 1))
            .photoUrl(photoUrl)
            .gpsLat(req.gpsLat()).gpsLng(req.gpsLng())
            .plantedDate(req.plantedDate()).notes(req.notes())
            .build();
        recordRepo.save(record);

        booking.setStatus(BookingStatus.PLANTED);
        bookingRepo.save(booking);

        certService.generateCertificates(bookingId);

        log.info("Plantation recorded for booking {} by official {}", booking.getBookingRef(), officialId);
        return toResponse(record);
    }

    @Transactional(readOnly = true)
    public List<CompletedPlantationResponse> getCompletedPlantations(String officalSystemCode, UUID parkId,
    		String officerRoleCode, LocalDate fromDate, LocalDate toDate) {
        List<PlantationRecord> records;
        //??
        if (officerRoleCode.equals(OfficialRole.R_HORTIC_ADM.name())) {
            records = (parkId != null)
                ? recordRepo.findByParkIds(List.of(parkId), fromDate, toDate)
                : recordRepo.findAllWithDetails(fromDate, toDate);
        } else {
            List<UUID> assigned = assignmentRepo.findParkIdsByOfficialId(officalSystemCode);
            if (assigned.isEmpty()) return List.of();
            List<UUID> targets = (parkId != null)
                ? (assigned.contains(parkId) ? List.of(parkId) : List.of())
                : assigned;
            records = targets.isEmpty() ? List.of() : recordRepo.findByParkIds(targets, fromDate, toDate);
        }
        if (records.isEmpty()) return List.of();

        List<UUID> bookingIds = records.stream()
            .map(r -> r.getBooking().getBookingId()).collect(Collectors.toList());
        // Ek booking ke liye pehli certificate lo (display ke liye)
        Map<UUID, Certificate> certMap = certRepo.findByBookingBookingIdIn(bookingIds).stream()
            .collect(Collectors.toMap(
                c -> c.getBooking().getBookingId(),
                c -> c,
                (existing, replacement) -> existing  // pehli certificate rakho
            ));

        return records.stream()
            .map(r -> toCompletedResponse(r, certMap.get(r.getBooking().getBookingId())))
            .collect(Collectors.toList());
    }

    // ── mappers ──────────────────────────────────────────────────────────────

	/*
	 * private CompletedPlantationResponse toCompletedResponse(PlantationRecord r,
	 * Certificate cert) { Booking b = r.getBooking(); SlotTreeInventory inv =
	 * b.getInventory(); return new CompletedPlantationResponse( r.getRecordId(),
	 * b.getBookingId(), b.getBookingRef(), // ?? b.getCitizen().getFullName(),
	 * "Pushpendra Singh", inv.getSlot().getPark().getParkId(),
	 * inv.getSlot().getPark().getName(), inv.getSpecies().getCommonName(),
	 * inv.getSpecies().getEmojiCode(), inv.getSlot().getSlotDate(),
	 * inv.getSlot().getStartTime(), r.getPlantedDate(), r.getTreeTagId(),
	 * r.getPhotoUrl(), r.getOfficial() != null ? "Officer" : null, cert != null ?
	 * cert.getCertNumber() : null, cert != null ? cert.getPdfUrl() : null, cert !=
	 * null && cert.isValid(), r.getRecordedAt() ); }
	 */

    
    private CompletedPlantationResponse toCompletedResponse(
            PlantationRecord record,
            Certificate certificate) {

        Booking booking = record.getBooking();

        ParkSlot slot = booking.getSlot();

        List<CompletedPlantationItemResponse> trees =
                booking.getItems()
                        .stream()
                        .map(item -> {

                            TreeSpecies species =
                                    item.getInventory().getSpecies();

                            return new CompletedPlantationItemResponse(

                                    species.getSpeciesId(),

                                    species.getCommonName(),

                                    species.getEmojiCode(),

                                    item.getQuantity());

                        })
                        .toList();

        return new CompletedPlantationResponse(

                record.getRecordId(),

                booking.getBookingId(),

                booking.getBookingRef(),

                swagamService.fetchUserSummary(booking.getCitizen(), UserTypeCode.UT_CTZ.name()).fullName(),

                slot.getPark().getParkId(),

                slot.getPark().getName(),
                
                trees,

                slot.getSlotDate(),

                slot.getStartTime(),

                record.getPlantedDate(),

                record.getTreeTagId(),

                record.getPhotoUrl(),

                record.getOfficial() != null ? "Officer" : null,

                certificate != null
                        ? certificate.getCertNumber()
                        : null,

                certificate != null
                        ? certificate.getPdfUrl()
                        : null,

                certificate != null && certificate.isValid(),

                record.getRecordedAt());
    }
    
    
    
	/*
	 * private PlantationRecordResponse toResponse(PlantationRecord r) { Booking b =
	 * r.getBooking(); SlotTreeInventory inv = b.getInventory(); return new
	 * PlantationRecordResponse( r.getRecordId(), b.getBookingRef(),
	 * //??b.getCitizen().getFullName(), "Pushpendra Singh",
	 * inv.getSlot().getPark().getName(), inv.getSlot().getStartTime(),
	 * inv.getSpecies().getCommonName(), r.getTreeTagId(), r.getPhotoUrl(),
	 * r.getGpsLat(), r.getGpsLng(), r.getPlantedDate(), r.getOfficial() != null ?
	 * "Officer" : null, r.getRecordedAt()); }
	 */
    
    
    private PlantationRecordResponse toResponse(PlantationRecord record) {

        Booking booking = record.getBooking();

        ParkSlot slot = booking.getSlot();

        List<PlantationRecordTreeResponse> trees =
                booking.getItems()
                        .stream()
                        .map(item -> {

                            TreeSpecies species = item.getInventory().getSpecies();

                            return new PlantationRecordTreeResponse(

                                    species.getSpeciesId(),

                                    species.getCommonName(),

                                    species.getEmojiCode(),

                                    item.getQuantity()

                            );
                        })
                        .toList();

        return new PlantationRecordResponse(

                record.getRecordId(),

                booking.getBookingRef(),

                swagamService.fetchUserSummary(booking.getCitizen(), UserTypeCode.UT_CTZ.name()).fullName(),

                slot.getPark().getName(),

                slot.getStartTime(),

                trees,

                record.getTreeTagId(),

                record.getPhotoUrl(),

                record.getGpsLat(),

                record.getGpsLng(),

                record.getPlantedDate(),

                record.getOfficial() != null
                        ? "Officer"
                        : null,

                record.getRecordedAt()

        );
    }
}
