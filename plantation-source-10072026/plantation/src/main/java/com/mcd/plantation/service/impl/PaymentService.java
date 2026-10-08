package com.mcd.plantation.service.impl;

import com.mcd.plantation.dto.request.PaymentVerifyRequest;
import com.mcd.plantation.dto.request.PgFinCalculation;
import com.mcd.plantation.dto.request.PgFinComponents;
import com.mcd.plantation.dto.request.PgFinDetailRequest;
import com.mcd.plantation.dto.request.PgInitRequest;
import com.mcd.plantation.dto.request.PgJson;
import com.mcd.plantation.dto.response.PaymentOrderResponse;
import com.mcd.plantation.entity.Booking;
import com.mcd.plantation.entity.Payment;
import com.mcd.plantation.entity.PaymentTransaction;
import com.mcd.plantation.enums.PaymentStatus;
import com.mcd.plantation.exception.PaymentVerificationException;
import com.mcd.plantation.repository.BookingRepository;
import com.mcd.plantation.repository.PaymentRepository;
import com.mcd.plantation.repository.PaymentTransactionRepository;
import com.mcd.plantation.util.GenerateUtil;
import com.mcd.plantation.util.JsonUtil;
import com.mcd.plantation.util.PgAESUtilityClient;
import com.mcd.plantation.util.PgUtil;
import com.razorpay.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service @RequiredArgsConstructor @Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepo;
    private final BookingRepository bookingRepo;
    private final PaymentTransactionRepository paymentTransactionRepository;

    @Value("${razorpay.key-id}")          private String keyId;
    @Value("${razorpay.key-secret}")      private String keySecret;
    @Value("${razorpay.processing-fee}")  private BigDecimal processingFee;
    @Value("${razorpay.dev-mode:true}")   private boolean devMode;
    
    @Value("${pg.initiate-url}")          private String initiateUrl;
    @Value("${pg.callback-url}")          private String callbackUrl;
    @Value("${pg.cancel-url}")            private String cancelUrl;
    /**
     * Optional overrides for where the gateway sends the citizen back. Blank
     * (the default) keeps callback-url / cancel-url above, so an existing
     * staging integration is untouched; set PG_RETURN_URL to land the citizen
     * on the Angular app instead (…/citizen/payment/result).
     */
    @Value("${pg.return-url:}")           private String returnUrlOverride;
    @Value("${pg.cancel-return-url:}")    private String cancelReturnUrlOverride;
    @Value("${pg.aes-key}")          	  private String aesKey;
    @Value("${pg.request-type}")          private String requestType;
    @Value("${pg.app-code}")              private String appCode;
    @Value("${pg.server-loacal-flag}")    private String pgServerFlag;
    
    private static final String PG_FLAG_SERVER    = "S"; // L is for local
    

    public PaymentOrderResponse createOrder(UUID bookingId, BigDecimal price,
                                            BigDecimal gst, BigDecimal total) {
        if (devMode) {
            log.warn("[DEV] Skipping Razorpay order creation for booking {}", bookingId);
            String fakeOrderId = "dev_order_" + bookingId.toString().replace("-", "").substring(0, 16);
            Booking booking = bookingRepo.findById(bookingId).orElseThrow();
            
            
            String     txnId = GenerateUtil.getDateTimeRandomNum(0, "");
            
            PgFinDetailRequest pgFinDetailRequest = toPgFinDetailRequest(bookingId, total, txnId);
            PgInitRequest pgInitRequest = toPgInitRequest(pgFinDetailRequest, txnId, total, bookingId);
            PgJson pgJson = toPgJson(pgInitRequest);
            
            String encrypted = new PgAESUtilityClient()
	                .encrypt(JsonUtil.convertObjectToJson(pgJson),
	                		aesKey);
            
            String postParam = "param=" + encrypted
	                + "&applicationReturnUrl=" + pgInitRequest.returnUrl()
	                + "&applicationCancelUrl="  + pgInitRequest.cancelUrl()
	                + "&uref="                  + GenerateUtil.getDateTimeRandomNum(0, "");
            
            String rawResponse;
			try {
				rawResponse = PG_FLAG_SERVER.equals(pgServerFlag)
				        ? PgUtil.sendPOST(initiateUrl, postParam, 
				        		new PgAESUtilityClient()
				                .encrypt(JsonUtil.convertObjectToJson(pgFinDetailRequest),
				                         aesKey))
				        : PgUtil.sendHttpPOST(initiateUrl, postParam,
				        		new PgAESUtilityClient()
				                .encrypt(JsonUtil.convertObjectToJson(pgFinDetailRequest),
				                		aesKey));
				if (rawResponse == null)
	            {
	            	throw new PaymentVerificationException("Payment gateway: Cannot Connect, response is empty");
	            }
			} catch (Exception e) {
				e.printStackTrace();
				throw new PaymentVerificationException("Payment gateway exception. Cannot Connect");
			} 
            
            
            
            PaymentTransaction paymentTransaction = PaymentTransaction.builder()
            		.paymentTransactionGuid(UUID.randomUUID().toString())
            		.paymentTransactionNumber(txnId)
            		.booking(booking)
            		.amount(total.toString())
            		// jsonb column: keep the gateway's initiate reply (and any
            		// browser redirect URL found in it) so the payment-init call
            		// can hand the citizen its URL later. Never logged.
            		.pgResponseJson(toInitResponseJson(rawResponse))
            		.isActive(true)
            		.createdBy(bookingId.toString()) // ???
            		.createdDate(new Date())
            		.createdIpAddr("10") // ??
            		.build();
            paymentTransactionRepository.save(paymentTransaction);
            
            Payment payment = Payment.builder()
                .booking(booking)
                .amount(price)
                .gstAmount(gst)
                .feeAmount(processingFee)
                .status(PaymentStatus.PENDING)
                .build();
            paymentRepo.save(payment);
            
            return new PaymentOrderResponse(fakeOrderId, "INR", price, gst, processingFee, total, "dev_key",
            		extractRedirectUrl(rawResponse));
        }

        try {
            RazorpayClient client = new RazorpayClient(keyId, keySecret);
            JSONObject opts = new JSONObject()
                .put("amount", total.multiply(BigDecimal.valueOf(100)).intValue())
                .put("currency", "INR")
                .put("receipt", "MCD-" + bookingId.toString().substring(0, 8))
                .put("payment_capture", 1);

            Order order = client.orders.create(opts);

            Booking booking = bookingRepo.findById(bookingId).orElseThrow();
            Payment payment = Payment.builder()
                .booking(booking)
                .amount(price)
                .gstAmount(gst)
                .feeAmount(processingFee)
                .status(PaymentStatus.PENDING)
                .build();
            paymentRepo.save(payment);

            return new PaymentOrderResponse(order.get("id"), "INR", price, gst, processingFee, total, keyId, "");
        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Payment gateway error. Please try again.");
        }
    }

    private PgJson toPgJson(PgInitRequest pgInitRequest) {
		
		return new PgJson(
							pgInitRequest.amount().toString(), 
							requestType, "", pgInitRequest.transactionId(), pgInitRequest.applicantName(), 
							appCode, "PLANTATION", "U"
						);
	}

	private PgInitRequest toPgInitRequest(PgFinDetailRequest pgFinDetailRequest, 
    		String txnId, BigDecimal total, UUID bookingId) {
		
		return new PgInitRequest(
				appCode, "Name", txnId, total, 
				// PG_RETURN_URL / PG_CANCEL_URL win when set, otherwise the
				// configured staging callback stays exactly as it was.
				effectiveReturnUrl(), effectiveCancelUrl(), bookingId, pgFinDetailRequest);
	}

	private String effectiveReturnUrl() {
		return (returnUrlOverride == null || returnUrlOverride.isBlank()) ? callbackUrl : returnUrlOverride.trim();
	}

	private String effectiveCancelUrl() {
		return (cancelReturnUrlOverride == null || cancelReturnUrlOverride.isBlank()) ? cancelUrl
				: cancelReturnUrlOverride.trim();
	}

	/**
	 * The gateway's initiate reply goes into the {@code pg_response_json} jsonb
	 * column, so it has to be valid JSON whatever the gateway sent. The reply is
	 * never logged and never returned as-is — only {@code redirectUrl} (if one
	 * can be found) is handed to the browser by POST /bookings/{id}/payment/init.
	 */
	private String toInitResponseJson(String rawResponse) {
		if (rawResponse == null || rawResponse.isBlank()) {
			return null;
		}
		Map<String, String> node = new LinkedHashMap<>();
		node.put("initResponse", rawResponse);
		String redirectUrl = extractRedirectUrl(rawResponse);
		if (redirectUrl != null) {
			node.put("redirectUrl", redirectUrl);
		}
		return JsonUtil.createJsonObjectText(node);
	}

	/**
	 * Finds a browser-openable payment URL in a gateway reply: the reply itself
	 * is a plain http(s) URL, or JSON carrying a url-ish field (top level or
	 * nested). Anything else yields {@code null} instead of guessing.
	 */
	private static String extractRedirectUrl(String rawResponse) {
		if (rawResponse == null) {
			return null;
		}
		String trimmed = rawResponse.trim();
		if (trimmed.matches("(?i)^https?://\\S+$")) {
			return trimmed;
		}
		if (!JsonUtil.isJsonObject(trimmed)) {
			return null;
		}
		return findUrlField(new JSONObject(trimmed));
	}

	private static String findUrlField(JSONObject json) {
		for (String key : json.keySet()) {
			Object value = json.opt(key);
			if (value instanceof JSONObject nested) {
				String found = findUrlField(nested);
				if (found != null) {
					return found;
				}
			} else if (value instanceof String text) {
				String lowerKey = key.toLowerCase();
				boolean urlLikeKey = lowerKey.contains("url") || lowerKey.contains("redirect")
						|| lowerKey.contains("href") || lowerKey.contains("link");
				if (urlLikeKey && text.trim().matches("(?i)^https?://\\S+$")) {
					return text.trim();
				}
			}
		}
		return null;
	}

	private PgFinDetailRequest toPgFinDetailRequest(UUID bookingId, BigDecimal total, String txnId) {
    	
    	PgFinComponents pgFinComponents =   new 
    			PgFinComponents(total.toString());
    	
    	Map<String, String> fyParts = getFinancialYearParts();
    	
		return new PgFinDetailRequest(
				appCode, 
				txnId, 
				"", 
				total.toString(), 
				"initiated", 
				"GIFT A TREE", 
				new Date().toString(), 
				"", 
				"", 
				"HQ", 
				UUID.randomUUID().toString(), 
				List.of(new PgFinCalculation(
						fyParts.get("financialYear"), "ONE TIME", 
						fyParts.get("periodFrom"), fyParts.get("periodTo"), 
				        total.toString(), total.toString(), "", "", pgFinComponents
				    ))
				);
	}

	/*
	 * public void verifyPayment(PaymentVerifyRequest req) { if (devMode) {
	 * log.warn("[DEV] Skipping Razorpay signature verification for order {}",
	 * req.razorpayOrderId()); return; }
	 * 
	 * try { String payload = req.razorpayOrderId() + "|" + req.razorpayPaymentId();
	 * Mac mac = Mac.getInstance("HmacSHA256"); mac.init(new
	 * SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
	 * String computed =
	 * bytesToHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8))); if
	 * (!computed.equals(req.razorpaySignature())) throw new
	 * PaymentVerificationException("Payment signature verification failed."); }
	 * catch (PaymentVerificationException e) { throw e; } catch (Exception e) {
	 * throw new PaymentVerificationException("Signature verification error: " +
	 * e.getMessage()); } }
	 */

	/*
	 * public void initiateRefund(UUID bookingId) {
	 * paymentRepo.findByBookingBookingId(bookingId).ifPresent(p -> { try {
	 * RazorpayClient client = new RazorpayClient(keyId, keySecret); JSONObject opts
	 * = new JSONObject() .put("amount",
	 * p.getTotalAmount().multiply(BigDecimal.valueOf(100)).intValue());
	 * client.payments.refund(p.getGatewayRef(), opts);
	 * p.setStatus(PaymentStatus.REFUNDED); paymentRepo.save(p);
	 * log.info("Refund initiated for payment {}", p.getPaymentId()); } catch
	 * (RazorpayException e) { log.error("Refund failed for payment {}: {}",
	 * p.getPaymentId(), e.getMessage()); } }); }
	 */
    
    private static Map<String, String> getFinancialYearParts() {
	    LocalDate today = LocalDate.now();
	    int currentYear = today.getYear();
	    int currentMonth = today.getMonthValue();

	    int fyStartYear = (currentMonth >= 4) ? currentYear : currentYear - 1;
	    int fyEndYear = fyStartYear + 1;

	    Map<String, String> fyMap = new LinkedHashMap<>();
	    fyMap.put("financialYear", fyStartYear + "-" + String.format("%02d", fyEndYear % 100)); // "2024-25"
	    fyMap.put("periodFrom", String.valueOf(fyStartYear));   // "2024"
	    fyMap.put("periodTo", String.valueOf(fyEndYear));       // "2025"

	    return fyMap;
	}

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

	public void verifyPayment(PaymentTransaction paymentTransaction, String statusCode, String loginUserSystemCode,
			String ipAddr, String decoded, String statusDesc) {
		
		if ("000".equals(statusCode)) {
			paymentTransaction.setIsActive(true);
			paymentTransaction.setPgStatusCode(statusCode);
			paymentTransaction.setPgResponseJson(decoded);
			paymentTransaction.setModifiedBy(loginUserSystemCode);
			paymentTransaction.setModifiedIpAddr(ipAddr);
			paymentTransaction.setModifiedDate(new Date());
			paymentTransaction.setPaymentTransactionStatus(statusDesc);
			paymentTransactionRepository.save(paymentTransaction);
		}
		if ("101".equals(statusCode)) {
			paymentTransaction.setIsActive(true);
			paymentTransaction.setPaymentTransactionStatus(statusDesc);
			paymentTransactionRepository.save(paymentTransaction);
		}
		else
		{
			paymentTransaction.setIsActive(false);
			paymentTransaction.setPaymentTransactionStatus(statusDesc);
			paymentTransactionRepository.save(paymentTransaction);
		}
		
	}
}
