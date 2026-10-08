package com.mcd.plantation.pojo;

public final class ErrorCode {

	private String code;
	private String message;
	
	private ErrorCode() {}
	
	private ErrorCode(String code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public static final ErrorCode ERROR = new ErrorCode("ERR_700", "ERROR IN REPOSITORY LAYER");
	public static final ErrorCode PERSIST_LAYER_ERROR = new ErrorCode("ERR_701", "ERROR IN REPOSITORY LAYER");
	public static final ErrorCode SERVICE_LAYER_ERROR = new ErrorCode("ERR_702", "ERROR IN SERVICE LAYER");
	public static final ErrorCode VALIDATION_ERROR = new ErrorCode("ERR_703", "ERROR IN VALIDATING DATA");
	public static final ErrorCode FILE_IO_ERROR = new ErrorCode("ERR_704", "ERROR IN FILE INPUT OUTPUT OPERATION");
	public static final ErrorCode TOKEN_ERROR = new ErrorCode("ERR_600", "ERROR IN TOKEN OPERATION");
	public static final ErrorCode TOKEN_VALIDATION_ERROR = new ErrorCode("ERR_601", "ERROR IN TOKEN VALIDITY OPERATION");

	public String getCode() {
		return code;
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public ErrorCode changeMessage(String message) {
		this.message = message;
		return this;
	}
}