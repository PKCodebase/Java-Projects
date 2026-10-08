package com.mcd.plantation.controller;

import com.mcd.plantation.dto.request.*;
import com.mcd.plantation.dto.response.*;
import com.mcd.plantation.pojo.UserToken;
import com.mcd.plantation.service.impl.*;
import com.mcd.plantation.util.HttpUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// ════════════════════════════════════════════════════════════════
//  BOOKING CONTROLLER  — /bookings
// ════════════════════════════════════════════════════════════════
@RestController @RequestMapping("/bookings") @RequiredArgsConstructor
@Tag(name = "Bookings") 
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    //@PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "Create booking — reserves slot-level inventory and returns payment order")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
    		Authentication authentication,
            @Valid @RequestBody CreateBookingRequest req) {
    	
    	String loginUserSystemCode = authentication != null ? 
				(authentication.getPrincipal() != null ? 
						((UserToken)authentication.getPrincipal()).getUserSystemCode() : null) 
				: null;
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok("Booking created", bookingService.createBooking(req, loginUserSystemCode)));
    }

    @GetMapping("/mine")
    //@PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "Get all bookings for the authenticated citizen")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings(
    		Authentication authentication) {
    	
    	String loginUserSystemCode = authentication != null ? 
				(authentication.getPrincipal() != null ? 
						((UserToken)authentication.getPrincipal()).getUserSystemCode() : null) 
				: null;
        return ResponseEntity.ok(ApiResponse.ok(bookingService.getMyBookings(loginUserSystemCode)));
    }

    @GetMapping("/{bookingId}")
    //@PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "Get a single booking by ID")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(
    		Authentication authentication,
            @PathVariable UUID bookingId
            ) {
    	String loginUserSystemCode = authentication != null ? 
				(authentication.getPrincipal() != null ? 
						((UserToken)authentication.getPrincipal()).getUserSystemCode() : null) 
				: null;
        return ResponseEntity.ok(ApiResponse.ok(bookingService.getBooking(bookingId, loginUserSystemCode)));
    }

    @PutMapping("/{bookingId}/presence")
    //@PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "Set presence mode (PHYSICAL or DELEGATED) after payment")
    public ResponseEntity<ApiResponse<BookingResponse>> setPresence(
    		Authentication authentication,
            @PathVariable UUID bookingId,
            @Valid @RequestBody SetPresenceModeRequest req) {
    	String loginUserSystemCode = authentication != null ? 
				(authentication.getPrincipal() != null ? 
						((UserToken)authentication.getPrincipal()).getUserSystemCode() : null) 
				: null;
    	
        return ResponseEntity.ok(ApiResponse.ok(
            bookingService.setPresenceMode(bookingId, req, loginUserSystemCode)));
    }

    @DeleteMapping("/{bookingId}")
    //@PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "Cancel a booking — releases slot-level inventory")
    public ResponseEntity<ApiResponse<Void>> cancel(
    		Authentication authentication,
            @PathVariable UUID bookingId
            ) {
    	
    	String loginUserSystemCode = authentication != null ? 
				(authentication.getPrincipal() != null ? 
						((UserToken)authentication.getPrincipal()).getUserSystemCode() : null) 
				: null;
    	
        bookingService.cancelBooking(bookingId, loginUserSystemCode);
        return ResponseEntity.ok(ApiResponse.ok("Booking cancelled successfully", null));
    }

    @PostMapping("/{bookingId}/payment/init")
    //@PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "Initialize Payment, returns url that needs to be redirected at MCD Payment Gateway")
    public ResponseEntity<ApiResponse<InitPaymentResponse>> initializePayment(
    		@PathVariable UUID bookingId,
    		Authentication authentication) {
    	
    	String loginUserSystemCode = authentication != null ? 
				(authentication.getPrincipal() != null ? 
						((UserToken)authentication.getPrincipal()).getUserSystemCode() : null) 
				: null;
        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.ok("Payment session ready", bookingService.initializePayment(bookingId, loginUserSystemCode)));
    }
    
    
    @PostMapping("/payment/verify")
    //@PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "Verify MCD gateway payment response")
    public ResponseEntity<ApiResponse<BookingResponse>> verifyPayment(
    		@RequestParam(value = "encryptedResponse", required = false) String encryptedResponse,
    		@RequestBody(required = false) Map<String, String> body,
            Authentication authentication,
            HttpServletRequest httpServletRequest) {
    	
    	String loginUserSystemCode = authentication != null ? 
				(authentication.getPrincipal() != null ? 
						((UserToken)authentication.getPrincipal()).getUserSystemCode() : null) 
				: null;
    	
    	String ipAddr = HttpUtils.getRequestIP(httpServletRequest);
    	
    	// Be liberal: the gateway may hand the payload back as a query string or
    	// as a form/JSON body, and the Angular return route posts it either way.
    	String payload = (encryptedResponse != null && !encryptedResponse.isBlank())
    			? encryptedResponse
    			: (body != null ? body.get("encryptedResponse") : null);
    	if (payload == null || payload.isBlank())
    		throw new IllegalArgumentException(
    				"Missing payment response: pass encryptedResponse as a query parameter or body field");
				
        return ResponseEntity.ok(ApiResponse.ok("Payment confirmed",
            bookingService.confirmPayment(payload, loginUserSystemCode, ipAddr)));
    }

    // ── TEST MODE (PG_TEST_MODE=true) ────────────────────────────────
    // Mints a clearly-labelled fake gateway response for the booking so a
    // demo/testing payment can complete without the real MCD gateway. The
    // payload is consumed by the REAL /payment/verify endpoint above.
    // Gated server-side: with pg.test-mode off this answers 404.
    @PostMapping("/{bookingId}/payment/test-payload")
    @Operation(summary = "TEST MODE ONLY (PG_TEST_MODE=true) — mint a fake gateway response for a booking")
    public ResponseEntity<ApiResponse<Map<String, String>>> testPaymentPayload(
    		@PathVariable UUID bookingId,
    		@RequestParam(name = "result", defaultValue = "success") String result,
            Authentication authentication) {
    	
    	String loginUserSystemCode = authentication != null ? 
				(authentication.getPrincipal() != null ? 
						((UserToken)authentication.getPrincipal()).getUserSystemCode() : null) 
				: null;
    	
        return ResponseEntity.ok(ApiResponse.ok("Test payment payload created",
            bookingService.buildTestPaymentPayload(bookingId, loginUserSystemCode, result)));
    }
}