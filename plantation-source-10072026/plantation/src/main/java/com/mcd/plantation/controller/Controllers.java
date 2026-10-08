package com.mcd.plantation.controller;

import com.mcd.plantation.dto.request.*;
import com.mcd.plantation.dto.response.*;
import com.mcd.plantation.service.impl.AuthService;
import com.mcd.plantation.service.impl.GoogleTokenVerifier;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// ════════════════════════════════════════════════════════════════
//  AUTH CONTROLLER  — /auth   (all endpoints are anonymous)
//
//  The three portal roles are bound end-to-end here:
//    • citizen register/login  → ROLE_CITIZEN
//    • official login          → ROLE_R_HORTIC_ADM (Horticulture Admin)
//                               / ROLE_R_HORTIC_OFF (Horticulture Officer)
//                               / ROLE_SUPERVISOR
//    • refresh                 → new pair, account re-checked in DB
// ════════════════════════════════════════════════════════════════
@RestController @RequestMapping("/auth") @RequiredArgsConstructor
@Tag(name = "Authentication")
class AuthController {

    private final AuthService authService;
    private final GoogleTokenVerifier googleTokenVerifier;

    @PostMapping("/citizen/register")
    @Operation(summary = "Register a new citizen account — returns JWT")
    public ResponseEntity<ApiResponse<AuthResponse>> registerCitizen(
            @Valid @RequestBody CitizenRegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Registration successful", authService.registerCitizen(req)));
    }

    @PostMapping("/citizen/login")
    @Operation(summary = "Citizen login — returns JWT")
    public ResponseEntity<ApiResponse<AuthResponse>> loginCitizen(
            @Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Login successful", authService.loginCitizen(req)));
    }

    @PostMapping("/official/login")
    @Operation(summary = "MCD Official login (Horticulture Admin / Officer / Supervisor) — returns JWT")
    public ResponseEntity<ApiResponse<AuthResponse>> loginOfficial(
            @Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Login successful", authService.loginOfficial(req)));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Exchange a refresh token for a new access + refresh pair")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Token refreshed", authService.refresh(req.refreshToken())));
    }

    // ── Not yet wired to an outbound channel ────────────────────────
    //
    // These flows need an external delivery channel (SMTP, SMS gateway).
    // Neither is configured in this deployment, so they answer 501 with an
    // explicit message instead of pretending to work — no OTP or password
    // reset link is ever fabricated.
    //
    // Google sign-in (below) is different: it IS implemented end to end and
    // only answers 501 while the GOOGLE_CLIENT_ID env var is unset.

    private static final String MAIL_NOT_CONFIGURED =
            "Password reset by e-mail is not enabled on this deployment. "
            + "Configure SMTP first (set MAIL_HOST, MAIL_PORT, MAIL_USER and MAIL_PASS, "
            + "then enable spring.mail in application.yml).";

    private static final String OTP_NOT_CONFIGURED =
            "Mobile OTP login is not enabled on this deployment — no SMS gateway is configured.";

    private static final String OAUTH_NOT_CONFIGURED =
            "Google sign-in is not enabled on this deployment — the GOOGLE_CLIENT_ID environment "
            + "variable is not set. Create an OAuth2 \"Web application\" client in the Google Cloud "
            + "console, export GOOGLE_CLIENT_ID and restart the backend.";

    @PostMapping("/forgot-password")
    @Operation(summary = "Starts the forgot-password flow (501 until SMTP is configured)")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestBody Map<String, String> body) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(ApiResponse.error(MAIL_NOT_CONFIGURED));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Confirms the forgot-password OTP (501 until SMTP is configured)")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody Map<String, String> body) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(ApiResponse.error(MAIL_NOT_CONFIGURED));
    }

    @PostMapping("/otp/send")
    @Operation(summary = "Sends a login OTP (501 until an SMS gateway is configured)")
    public ResponseEntity<ApiResponse<Void>> sendOtp(@RequestBody Map<String, String> body) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(ApiResponse.error(OTP_NOT_CONFIGURED));
    }

    @PostMapping("/otp/login")
    @Operation(summary = "Logs in with an OTP (501 until an SMS gateway is configured)")
    public ResponseEntity<ApiResponse<Void>> otpLogin(@RequestBody Map<String, String> body) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(ApiResponse.error(OTP_NOT_CONFIGURED));
    }

    @PostMapping("/oauth2/google")
    @Operation(summary = "Exchanges a Google ID token for a JWT (501 until GOOGLE_CLIENT_ID is configured)")
    public ResponseEntity<ApiResponse<AuthResponse>> oauth2Google(@Valid @RequestBody GoogleCredentialRequest req) {
        if (!googleTokenVerifier.isEnabled()) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(ApiResponse.error(OAUTH_NOT_CONFIGURED));
        }
        GoogleTokenVerifier.Profile profile = googleTokenVerifier.verify(req.credential());
        return ResponseEntity.ok(ApiResponse.ok("Login successful", authService.loginWithGoogle(profile)));
    }

    /**
     * Public Google sign-in configuration. Lets the UI distinguish "not
     * configured" (button disabled with an explanation) from a real failure,
     * and gives it the public client id to initialise Google Identity
     * Services — no secret is ever returned here.
     */
    @GetMapping("/oauth2/google/config")
    @Operation(summary = "Whether Google sign-in is configured + the public OAuth2 client id (never a secret)")
    public ResponseEntity<ApiResponse<OauthClientConfigResponse>> oauth2GoogleConfig() {
        return ResponseEntity.ok(ApiResponse.ok(new OauthClientConfigResponse(
                googleTokenVerifier.isEnabled(), googleTokenVerifier.clientId())));
    }
}
