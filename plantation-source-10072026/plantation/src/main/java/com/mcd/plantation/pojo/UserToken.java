package com.mcd.plantation.pojo;

import java.security.NoSuchAlgorithmException;

import com.mcd.plantation.util.AESUtils;
import com.mcd.plantation.util.GenerateUtil;


public final class UserToken {

	private String userGuid;
	private String userProfileGuid;
	private String authTypeCode;
	private String userSystemCode;
	private String userSpecifiedCode;
	private String loginId;
	private String emailId;
	private String mobileNumber;
	private String userTypeCode;
	private String roleCode;
	private String sid;
	private String sk;
	
	public UserToken() {
		this.sid = GenerateUtil.getAlpaNumId();
		try {
			this.sk = AESUtils.generateKeyAsString(AESUtils.KEY_SIZE_128);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	
	public UserToken(String userGuid, String userProfileGuid, String authTypeCode, String userSystemCode, String userSpecifiedCode, String loginId,
			String emailId, String mobileNumber, String userTypeCode, String roleCode) {
		this.userGuid = userGuid;
		this.userProfileGuid = userProfileGuid;
		this.authTypeCode = authTypeCode;
		this.userSystemCode = userSystemCode;
		this.userSpecifiedCode = userSpecifiedCode;
		this.loginId = loginId;
		this.emailId = emailId;
		this.mobileNumber = mobileNumber;
		this.userTypeCode = userTypeCode;
		this.roleCode = roleCode;
		this.sid = GenerateUtil.getAlpaNumId();
		try {
			this.sk = AESUtils.generateKeyAsString(AESUtils.KEY_SIZE_128);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	
	public UserToken(String userGuid, String userProfileGuid, String authTypeCode, String userSystemCode, String userSpecifiedCode, String loginId,
			String emailId, String mobileNumber, String userTypeCode, String roleCode, String sid, String sk) {
		this.userGuid = userGuid;
		this.userProfileGuid = userProfileGuid;
		this.authTypeCode = authTypeCode;
		this.userSystemCode = userSystemCode;
		this.userSpecifiedCode = userSpecifiedCode;
		this.loginId = loginId;
		this.emailId = emailId;
		this.mobileNumber = mobileNumber;
		this.userTypeCode = userTypeCode;
		this.roleCode = roleCode;
		this.sid = sid;
		this.sk = sk;
	}
	
	public String getUserGuid() {
		return userGuid;
	}

	public void setUserGuid(String userGuid) {
		this.userGuid = userGuid;
	}

	public String getUserProfileGuid() {
		return userProfileGuid;
	}

	public void setUserProfileGuid(String userProfileGuid) {
		this.userProfileGuid = userProfileGuid;
	}

	public String getAuthTypeCode() {
		return authTypeCode;
	}

	public void setAuthTypeCode(String authTypeCode) {
		this.authTypeCode = authTypeCode;
	}

	public String getUserSystemCode() {
		return userSystemCode;
	}

	public void setUserSystemCode(String userSystemCode) {
		this.userSystemCode = userSystemCode;
	}

	public String getUserSpecifiedCode() {
		return userSpecifiedCode;
	}

	public void setUserSpecifiedCode(String userSpecifiedCode) {
		this.userSpecifiedCode = userSpecifiedCode;
	}

	public String getLoginId() {
		return loginId;
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getUserTypeCode() {
		return userTypeCode;
	}

	public void setUserTypeCode(String userTypeCode) {
		this.userTypeCode = userTypeCode;
	}

	public String getRoleCode() {
		return roleCode;
	}

	public void setRoleCode(String roleCode) {
		this.roleCode = roleCode;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public String getSk() {
		return sk;
	}

	public void setSk(String sk) {
		this.sk = sk;
	}
}
