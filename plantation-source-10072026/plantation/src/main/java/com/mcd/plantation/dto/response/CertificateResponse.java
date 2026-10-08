package com.mcd.plantation.dto.response;

import com.mcd.plantation.enums.PresenceMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

// Per-tree certificate response (1 tree = 1 certificate)
public record CertificateResponse(
    UUID certId,
    String certNumber,
    String citizenName,
    String parkName,
    // Single tree info (not a list)
    UUID speciesId,
    String treeName,
    String treeEmoji,
    String scientificName,
    int quantity,
    int treeIndex,          // e.g. 2 of 3 for Neem
    LocalDate plantedDate,
    LocalTime slotTime,
    PresenceMode presenceMode,
    String photoUrl,
    String treeTagId,
    String pdfUrl,
    boolean isValid,
    OffsetDateTime issuedAt
) {}
