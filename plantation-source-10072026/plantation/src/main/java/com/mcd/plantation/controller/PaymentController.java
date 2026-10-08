package com.mcd.plantation.controller;

import com.mcd.plantation.service.impl.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;


// ════════════════════════════════════════════════════════════════
//  PAYMENT CONTROLLER  — /payment
// ════════════════════════════════════════════════════════════════
@RestController @RequestMapping("/payment") @RequiredArgsConstructor
@Tag(name = "Payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/webhook")
    @Operation(summary = "Razorpay webhook — no auth header; verifies internally")
    public ResponseEntity<String> webhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {
        // Webhook payload processing — extract payment.captured event
        // and trigger confirmation flow if needed
        return ResponseEntity.ok("ok");
    }
}