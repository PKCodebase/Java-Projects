package com.mcd.plantation.dto.response;

import com.mcd.plantation.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentStatusResponse(
    UUID paymentId,
    PaymentStatus status,
    BigDecimal totalAmount,
    String gatewayRef,
    OffsetDateTime paidAt
) {}
