package com.mcd.plantation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record CompletedPlantationResponse(
    UUID recordId,
    UUID bookingId,
    String bookingRef,
    String citizenName,
    UUID parkId,
    String parkName,
    List<CompletedPlantationItemResponse> trees,
    LocalDate slotDate,
    LocalTime slotTime,
    LocalDate plantedDate,
    String treeTagId,
    String photoUrl,
    String officialName,
    String certNumber,
    String pdfUrl,
    boolean certValid,
    OffsetDateTime recordedAt
) {}
