package com.mcd.plantation.service.impl;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.mcd.plantation.dto.response.CertificateResponse;
import com.mcd.plantation.dto.response.ReturnParam;
import com.mcd.plantation.entity.*;
import com.mcd.plantation.enums.BookingStatus;
import com.mcd.plantation.enums.UserTypeCode;
import com.mcd.plantation.exception.ResourceNotFoundException;
import com.mcd.plantation.pojo.CertificatePdf;
import com.mcd.plantation.repository.BookingRepository;
import com.mcd.plantation.repository.CertificateRepository;
import com.mcd.plantation.repository.PlantationRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateService {

	private final CertificateRepository certRepo;
	private final PlantationRecordRepository recordRepo;
	private final BookingRepository bookingRepo;
//	private final EmailService emailService; // EMAIL DISABLED
	private final FileStorageService fileStorage;
	private final SwagamService swagamService;
	private final PdfService pdfService;

	@Value("${storage.upload-dir}")
	private String uploadDir;

	private static final DeviceRgb GREEN_DARK = new DeviceRgb(34, 85, 34);
	private static final DeviceRgb GREEN_LIGHT = new DeviceRgb(232, 245, 232);
	private static final DeviceRgb GOLD = new DeviceRgb(180, 140, 40);
	private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMMM yyyy");

	// ── Generate & persist ────────────────────────────────────────────────────

	/**
	 * Har BookingItem ke liye, quantity ke hisab se alag-alag certificate generate karta hai.
	 * Example: Neem x2, Peepal x1 → 3 certificates total
	 */
	@Transactional
	public List<CertificateResponse> generateCertificates(UUID bookingId) {
		Booking booking = bookingRepo.findById(bookingId)
				.orElseThrow(() -> ResourceNotFoundException.of("Booking", bookingId));

		PlantationRecord record = recordRepo.findByBookingBookingId(bookingId)
				.orElseThrow(() -> ResourceNotFoundException.of("PlantationRecord", bookingId));

		List<CertificateResponse> responses = new java.util.ArrayList<>();

		for (com.mcd.plantation.entity.BookingItems item : booking.getItems()) {
			int qty = item.getQuantity();
			for (int idx = 1; idx <= qty; idx++) {
				String certNumber = generateCertNumber();
				byte[] pdf = buildPdf(booking, record, item, certNumber);

				String filename = certNumber.replace("/", "-") + ".pdf";
				String pdfUrl = fileStorage.storeBytes(pdf, "certificates", filename);

				Certificate cert = Certificate.builder()
						.booking(booking)
						.bookingItem(item)
						.treeIndex(idx)
						.record(record)
						.certNumber(certNumber)
						.pdfBucket("local")
						.pdfKey("certificates/" + filename)
						.pdfUrl(pdfUrl)
						.build();
				certRepo.save(cert);

				// EMAIL DISABLED — certificate save hoti rahegi, email nahi jayegi
				// Jab email enable karni ho tab neeche wala uncomment karo:
				// try {
				// 	// TODO: Production me citizen ki actual email use karni hai
				// 	// String citizenEmail = swagamService.fetchUserSummary(booking.getCitizen(), UserTypeCode.UT_CTZ.name()).email();
				// 	emailService.sendCertificate("kaushikprasad1659@gmail.com", cert);
				// } catch (Exception e) {
				// 	log.warn("Email send failed for cert {}: {}", certNumber, e.getMessage());
				// }
				log.info("Certificate issued: {} for booking {} tree {} [{}/{}]",
						certNumber, booking.getBookingRef(),
						item.getInventory().getSpecies().getCommonName(), idx, qty);
				responses.add(toCertResponse(cert));
			}
		}

		booking.setStatus(BookingStatus.COMPLETED);
		bookingRepo.save(booking);

		return responses;
	}

	private byte[] buildPdf(Booking booking, PlantationRecord record, BookingItems item, String certNumber) {
		try {
			String citizen = swagamService.fetchUserSummary(booking.getCitizen(), UserTypeCode.UT_CTZ.name()).fullName();
			String park = booking.getSlot().getPark().getName();
			String occasion = booking.getOccasion() != null ? booking.getOccasion().getName() : "Plantation";
			String date = record.getPlantedDate() != null
					? record.getPlantedDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "";

			CertificatePdf pdf = new CertificatePdf();
			pdf.setName(citizen);
			pdf.setOccasion(occasion);
			pdf.setPlace(park);
			pdf.setDate(date);

			ReturnParam result = pdfService.generate(pdf, "GIFT_TREE");
			if (!result.isStatus()) {
				throw new RuntimeException("Certificate generation failed: " + result.getMessage());
			}

			byte[] pdfBytes = Base64.getDecoder().decode(result.getValue());
			log.info("Certificate PDF generated from service: {}, size: {} bytes", certNumber, pdfBytes.length);
			return pdfBytes;

		} catch (Exception e) {
			log.error("Certificate generation failed for {}: {}", certNumber, e.getMessage());
			throw new RuntimeException("Error generating certificate from service", e);
		}
	}

	// ── Read ──────────────────────────────────────────────────────────────────

	// Ek booking ke saare certificates (tree x quantity)
	@Transactional(readOnly = true)
	public List<CertificateResponse> getCertificates(UUID bookingId) {
		List<Certificate> certs = certRepo.findByBookingBookingId(bookingId);
		if (certs.isEmpty())
			throw new ResourceNotFoundException("No certificates found for booking: " + bookingId);
		return certs.stream().map(this::toCertResponse).collect(Collectors.toList());
	}

	// Cert number se single certificate verify karo (public)
	@Transactional(readOnly = true)
	public CertificateResponse verifyCertificate(String certNumber) {
		return toCertResponse(certRepo.findByCertNumber(certNumber)
				.orElseThrow(() -> ResourceNotFoundException.of("Certificate", certNumber)));
	}

	// ── PDF generation ────────────────────────────────────────────────────────

//	private byte[] buildPdf(Booking b, PlantationRecord r, BookingItems item, int idx, int total, String certNumber) {
//
//		ParkSlot slot = b.getSlot();
//		String park = slot.getPark().getName();
//
//		TreeSpecies species = item.getInventory().getSpecies();
//		String tree = species.getCommonName();
//		String sciName = species.getScientificName();
//		String emoji = species.getEmojiCode() != null ? species.getEmojiCode() : "";
//
//		String citizen = swagamService.fetchUserSummary(b.getCitizen(), UserTypeCode.UT_CTZ.name()).fullName();
//		String official = r.getOfficial() != null
//				? swagamService.fetchUserSummary(r.getOfficial(), UserTypeCode.UT_EMP.name()).fullName()
//				: "MCD Officer";
//		LocalDate planted = r.getPlantedDate();
//
//		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
//			PdfWriter writer = new PdfWriter(baos);
//			PdfDocument pdfDoc = new PdfDocument(writer);
//			Document doc = new Document(pdfDoc, PageSize.A4);
//			doc.setMargins(48, 48, 48, 48);
//
//			PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
//			PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
//			PdfFont italic = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);
//
//			// ── Outer border ─────────────────────────────────────────────────
//			Table frame = new Table(UnitValue.createPercentArray(new float[] { 1 })).useAllAvailableWidth()
//					.setBorder(new SolidBorder(GOLD, 3)).setMarginBottom(0);
//			Cell frameCell = new Cell().setBorder(new SolidBorder(GREEN_DARK, 1)).setMargin(6)
//					.setBackgroundColor(GREEN_LIGHT);
//
//			// ── Header ───────────────────────────────────────────────────────
//			frameCell.add(new Paragraph("🌿 MUNICIPAL CORPORATION OF DELHI 🌿").setFont(bold).setFontSize(14)
//					.setFontColor(GREEN_DARK).setTextAlignment(TextAlignment.CENTER).setMarginBottom(2));
//
//			frameCell.add(new Paragraph("Horticulture Department — Green Plantation Drive").setFont(regular)
//					.setFontSize(10).setFontColor(ColorConstants.DARK_GRAY).setTextAlignment(TextAlignment.CENTER)
//					.setMarginBottom(12));
//
//			// ── Certificate title ─────────────────────────────────────────────
//			frameCell.add(new Paragraph("PLANTATION CERTIFICATE").setFont(bold).setFontSize(22).setFontColor(GOLD)
//					.setTextAlignment(TextAlignment.CENTER).setMarginBottom(4));
//
//			frameCell.add(new Paragraph("Certificate No: " + certNumber
//					+ "   |   Tree " + idx + " of " + total).setFont(italic).setFontSize(10)
//					.setFontColor(ColorConstants.GRAY).setTextAlignment(TextAlignment.CENTER).setMarginBottom(16));
//
//			// ── Body text ─────────────────────────────────────────────────────
//			frameCell.add(new Paragraph("This is to certify that").setFont(regular).setFontSize(12)
//					.setTextAlignment(TextAlignment.CENTER).setMarginBottom(6));
//
//			frameCell.add(new Paragraph(citizen).setFont(bold).setFontSize(18).setFontColor(GREEN_DARK)
//					.setTextAlignment(TextAlignment.CENTER).setMarginBottom(6));
//
//			frameCell.add(new Paragraph("has successfully participated in the MCD Green Plantation Drive by planting a")
//					.setFont(regular).setFontSize(12).setTextAlignment(TextAlignment.CENTER).setMarginBottom(6));
//
//			frameCell.add(new Paragraph(emoji + "  " + tree + "  (" + sciName + ")"
//					+ "  [" + idx + "/" + total + "]").setFont(bold).setFontSize(15)
//					.setFontColor(GREEN_DARK).setTextAlignment(TextAlignment.CENTER).setMarginBottom(6));
//
//			frameCell.add(new Paragraph("at  " + park).setFont(regular).setFontSize(12)
//					.setTextAlignment(TextAlignment.CENTER).setMarginBottom(4));
//
//			frameCell.add(new Paragraph("on  " + planted.format(DATE_FMT)).setFont(italic).setFontSize(12)
//					.setFontColor(ColorConstants.DARK_GRAY).setTextAlignment(TextAlignment.CENTER).setMarginBottom(16));
//
//			// ── Plantation photo ──────────────────────────────────────────────
//			if (r.getPhotoKey() != null) {
//				try {
//					Path photoPath = Paths.get(uploadDir, r.getPhotoKey());
//					if (Files.exists(photoPath)) {
//						ImageData imgData = ImageDataFactory.create(photoPath.toAbsolutePath().toString());
//						Image photo = new Image(imgData).setWidth(UnitValue.createPercentValue(55))
//								.setHorizontalAlignment(HorizontalAlignment.CENTER).setMarginBottom(4);
//						frameCell.add(new Paragraph("Plantation Photo").setFont(italic).setFontSize(9)
//								.setFontColor(ColorConstants.GRAY).setTextAlignment(TextAlignment.CENTER));
//						frameCell.add(photo);
//					}
//				} catch (Exception ex) {
//					log.warn("Could not embed photo in certificate: {}", ex.getMessage());
//				}
//			}
//
//			// ── Details table ────────────────────────────────────────────────
//			Table details = new Table(UnitValue.createPercentArray(new float[] { 1, 1 }))
//					.setWidth(UnitValue.createPercentValue(80)).setHorizontalAlignment(HorizontalAlignment.CENTER)
//					.setBorder(Border.NO_BORDER).setMarginBottom(24);
//
//			addDetailRow(details, bold, regular, "Booking Ref", b.getBookingRef());
//			addDetailRow(details, bold, regular, "Tree Tag ID", r.getTreeTagId() != null ? r.getTreeTagId() : "—");
//			addDetailRow(details, bold, regular, "Park", park);
//			addDetailRow(details, bold, regular, "Planted On", planted.format(DATE_FMT));
//			addDetailRow(details, bold, regular, "Recorded By", official);
//			addDetailRow(details, bold, regular, "Issued On", LocalDate.now().format(DATE_FMT));
//
//			frameCell.add(details);
//
//			// ── Footer ───────────────────────────────────────────────────────
//			frameCell.add(new Paragraph("This certificate is digitally issued by the MCD Horticulture Department. "
//					+ "Verify online using certificate number at the MCD Green portal.").setFont(italic).setFontSize(8)
//					.setFontColor(ColorConstants.GRAY).setTextAlignment(TextAlignment.CENTER).setMarginTop(8));
//
//			frame.addCell(frameCell);
//			doc.add(frame);
//			doc.close();
//			return baos.toByteArray();
//
//		} catch (IOException e) {
//			log.error("PDF generation failed for cert {}: {}", certNumber, e.getMessage());
//			throw new RuntimeException("Certificate PDF generation failed: " + e.getMessage(), e);
//		}
//	}
//
//	private void addDetailRow(Table table, PdfFont bold, PdfFont regular, String label, String value) {
//		table.addCell(new Cell().add(new Paragraph(label).setFont(bold).setFontSize(10)).setBorder(Border.NO_BORDER)
//				.setFontColor(GREEN_DARK));
//		table.addCell(
//				new Cell().add(new Paragraph(value).setFont(regular).setFontSize(10)).setBorder(Border.NO_BORDER));
//	}

	// ── Cert number ───────────────────────────────────────────────────────────

	private synchronized String generateCertNumber() {
		long count = certRepo.count() + 1;
		return "MCD/CERT/" + LocalDate.now().getYear() + "/" + String.format("%06d", count);
	}

	// ── Response mapper ───────────────────────────────────────────────────────

	private CertificateResponse toCertResponse(Certificate cert) {
		Booking booking = cert.getBooking();
		PlantationRecord record = cert.getRecord();
		ParkSlot slot = booking.getSlot();
		TreeSpecies species = cert.getBookingItem().getInventory().getSpecies();

		return new CertificateResponse(
				cert.getCertId(),
				cert.getCertNumber(),
				swagamService.fetchUserSummary(booking.getCitizen(), UserTypeCode.UT_CTZ.name()).fullName(),
				slot.getPark().getName(),
				species.getSpeciesId(),
				species.getCommonName(),
				species.getEmojiCode(),
				species.getScientificName(),
				cert.getBookingItem().getQuantity(),
				cert.getTreeIndex(),
				record != null ? record.getPlantedDate() : null,
				slot.getStartTime(),
				booking.getPresenceMode(),
				record != null ? record.getPhotoUrl() : null,
				record != null ? record.getTreeTagId() : null,
				cert.getPdfUrl(),
				cert.isValid(),
				cert.getIssuedAt()
		);
	}
}
