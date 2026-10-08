package com.mcd.plantation.controller;

import com.mcd.plantation.dto.response.*;
import com.mcd.plantation.service.impl.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;


// ════════════════════════════════════════════════════════════════
//  CERTIFICATE CONTROLLER  — /certificates
// ════════════════════════════════════════════════════════════════
@RestController @RequestMapping("/certificates") @RequiredArgsConstructor
@Tag(name = "Certificates")
public class CertificateController {

    private final CertificateService certService;

    // Ek booking ke saare certificates — jitni trees utni certificates
    @GetMapping("/{bookingId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get all certificates for a booking (one per tree x quantity)")
    public ResponseEntity<ApiResponse<List<CertificateResponse>>> getCertificates(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(ApiResponse.ok(certService.getCertificates(bookingId)));
    }

    // Public verify — cert number se single certificate
    @GetMapping("/verify/{certNumber}")
    @Operation(summary = "Public: verify a certificate by its number")
    public ResponseEntity<ApiResponse<CertificateResponse>> verifyCertificate(
            @PathVariable String certNumber) {
        return ResponseEntity.ok(ApiResponse.ok(certService.verifyCertificate(certNumber)));
    }
}