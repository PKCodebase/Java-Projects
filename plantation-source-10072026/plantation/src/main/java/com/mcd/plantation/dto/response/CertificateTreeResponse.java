package com.mcd.plantation.dto.response;

import java.util.UUID;

public record CertificateTreeResponse(

    UUID speciesId,

    String treeName,

    String treeEmoji,

    String scientificName,

    Integer quantity

) {}