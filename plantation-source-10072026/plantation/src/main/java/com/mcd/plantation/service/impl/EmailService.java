package com.mcd.plantation.service.impl;

import com.mcd.plantation.entity.Booking;
import com.mcd.plantation.entity.BookingItems;
import com.mcd.plantation.entity.Certificate;
import com.mcd.plantation.entity.ParkSlot;
import com.mcd.plantation.entity.SlotTreeInventory;
import com.mcd.plantation.entity.TreeSpecies;
import com.mcd.plantation.enums.UserTypeCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

// EMAIL SERVICE DISABLED — jab enable karna ho tab:
// 1. Neeche ke pure commented code ko uncomment karo
// 2. application.yml me mail: block uncomment karo
// 3. BookingService me emailService.sendBookingConfirmation(booking) uncomment karo
// 4. CertificateService me email try-catch block uncomment karo

// ════════════════════════════════════════════════════════════════
//  EMAIL SERVICE  (package-private — internal use only)
// ════════════════════════════════════════════════════════════════
//@Service @RequiredArgsConstructor @Slf4j
//class EmailService {
//
//    private final JavaMailSender mailSender;
//    private final SwagamService  swagamService;
//
//    @Value("${spring.mail.username}") private String from;
//
//    public void sendBookingConfirmation(Booking booking) {
//
//        ParkSlot slot = booking.getSlot();
//
//        String trees = booking.getItems()
//                .stream()
//                .map(item -> {
//                    TreeSpecies species = item.getInventory().getSpecies();
//                    return "%s %s x%d".formatted(
//                            species.getEmojiCode(),
//                            species.getCommonName(),
//                            item.getQuantity());
//                })
//                .collect(Collectors.joining("\n"));
//
//        String subject = "Booking Confirmed — " + booking.getBookingRef();
//
//        String body = """
//                Dear %s,
//
//                Your plantation booking has been confirmed!
//
//                Booking Reference : %s
//                Park              : %s
//                Date & Time       : %s at %s
//
//                Trees Booked
//                ------------------------
//                %s
//
//                Total Species     : %d
//                Total Plants      : %d
//
//                Please carry this reference when visiting the park.
//
//                Regards,
//                MCD Horticulture Department
//                """.formatted(
//                swagamService.fetchUserSummary(booking.getCitizen(), UserTypeCode.UT_CTZ.name()).fullName(),
//                booking.getBookingRef(),
//                slot.getPark().getName(),
//                slot.getSlotDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
//                slot.getStartTime(),
//                trees,
//                booking.getItems().size(),
//                booking.getItems().stream().mapToInt(BookingItems::getQuantity).sum());
//
//        // TODO: Production me citizen ki actual email use karni hai
//        // sendMail(booking.getCitizen().getEmail(), subject, body);
//        sendMail("kaushikprasad1659@gmail.com", subject, body);
//    }
//
//    public void sendCertificate(String email, Certificate cert) {
//        String subject = "Your Plantation Certificate — " + cert.getCertNumber();
//        String body = """
//            Dear Citizen,
//
//            Your government-issued plantation certificate has been generated!
//
//            Certificate Number : %s
//            Download PDF       : %s
//
//            Thank you for contributing to Delhi's green future.
//
//            Regards,
//            MCD Horticulture Department
//            """.formatted(cert.getCertNumber(), cert.getPdfUrl());
//        sendMail(email, subject, body);
//    }
//
//    private void sendMail(String to, String subject, String text) {
//        try {
//            var msg = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(msg, false, "UTF-8");
//            helper.setFrom(from);
//            helper.setTo(to);
//            helper.setSubject(subject);
//            helper.setText(text);
//            mailSender.send(msg);
//            log.info("Email sent to {}: {}", to, subject);
//        } catch (Exception e) {
//            log.error("Email send failed to {}: {}", to, e.getMessage());
//        }
//    }
//}
