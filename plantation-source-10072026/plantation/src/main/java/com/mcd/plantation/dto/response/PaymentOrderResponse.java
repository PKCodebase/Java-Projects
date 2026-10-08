package com.mcd.plantation.dto.response;

import java.math.BigDecimal;

public record PaymentOrderResponse(
    String orderId,
    String currency,
    BigDecimal amount,
    BigDecimal gstAmount,
    BigDecimal feeAmount,
    BigDecimal totalAmount,
    String keyId,
    String url
) {}
