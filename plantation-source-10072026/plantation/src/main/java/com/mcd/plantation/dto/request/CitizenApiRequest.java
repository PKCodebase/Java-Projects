package com.mcd.plantation.dto.request;

public record CitizenApiRequest(
    String userCode,
    String userGuid,
    String loginId,
    String mobileNumber,
    String emailId,
    String userTypeCode,
    String modifiedDate
) {}