package com.mcd.plantation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/*public record PlantationRecordResponse(
    UUID recordId,
    String bookingRef,
    String citizenName,
    String parkName,
    LocalTime slotTime,
    String treeName,
    String treeTagId,
    String photoUrl,
    Double gpsLat,
    Double gpsLng,
    LocalDate plantedDate,
    String officialName,
    OffsetDateTime recordedAt
) {}*/


public record PlantationRecordResponse(

	    UUID recordId,

	    String bookingRef,

	    String citizenName,

	    String parkName,

	    LocalTime slotTime,

	    List<PlantationRecordTreeResponse> trees,

	    String treeTagId,

	    String photoUrl,

	    Double gpsLat,

	    Double gpsLng,

	    LocalDate plantedDate,

	    String officialName,

	    OffsetDateTime recordedAt

	) {}
