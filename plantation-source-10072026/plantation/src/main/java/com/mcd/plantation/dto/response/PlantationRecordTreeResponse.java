package com.mcd.plantation.dto.response;

import java.util.UUID;

public record PlantationRecordTreeResponse(

    UUID speciesId,

    String treeName,

    String treeEmoji,

    Integer quantity

) {}