package com.mcd.plantation.dto.response;

import com.mcd.plantation.enums.BookingStatus;
import com.mcd.plantation.enums.PresenceMode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record BookingResponse(
    UUID bookingId,
    String bookingRef,
    String citizenName,
    String citizenEmail,
    String citizenPhone,
    String citizenAadhaarLast4,
    UUID slotId,
    UUID parkId,
    String parkName,
    //UUID speciesId,
    LocalDate slotDate,
    LocalTime slotTime,
    List<BookingItemResponse> items,
    //String treeName,
    //String treeEmoji,
    BookingStatus status,
    PresenceMode presenceMode,
    BigDecimal amountPaid,
    OffsetDateTime bookedAt,
    PaymentOrderResponse paymentOrder,
    UUID occasionId,
    String occasionName,
    String occasionEmoji
    
) {}
