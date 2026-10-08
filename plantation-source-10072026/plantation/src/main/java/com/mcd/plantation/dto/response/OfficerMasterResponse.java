package com.mcd.plantation.dto.response;

public record OfficerMasterResponse(
    String userCode,
    String userName,
    String primaryOrgCode,
    String wrapperOrgCode,
    String orgCode
) {}