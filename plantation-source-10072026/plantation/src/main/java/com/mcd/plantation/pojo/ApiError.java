package com.mcd.plantation.pojo;

import java.util.Date;

public class ApiError {

	private Date datetime ;
	private String httpStatus;
	private String httpMessage;
	private String path;
	private String errorCode;
	private String errorMessage;
	
	public ApiError(String httpStatus, String httpMessage, String path, String errorCode,
			String errorMessage) {
		super();
		this.datetime = new Date();
		this.httpStatus = httpStatus;
		this.httpMessage = httpMessage;
		this.path = path;
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}
	
	public ApiError(Date datetime, String httpStatus, String httpMessage, String path, String errorCode,
			String errorMessage) {
		super();
		this.datetime = datetime;
		this.httpStatus = httpStatus;
		this.httpMessage = httpMessage;
		this.path = path;
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public Date getDatetime() {
		return datetime;
	}

	public void setDatetime(Date datetime) {
		this.datetime = datetime;
	}

	public String getHttpStatus() {
		return httpStatus;
	}

	public void setHttpStatus(String httpStatus) {
		this.httpStatus = httpStatus;
	}

	public String getHttpMessage() {
		return httpMessage;
	}

	public void setHttpMessage(String httpMessage) {
		this.httpMessage = httpMessage;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}