package com.mcd.plantation.exception;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcd.plantation.pojo.ErrorCode;
import com.mcd.plantation.util.DateTimeUtil;
import com.mcd.plantation.util.GenerateUtil;


public class ServiceException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger("PERSIST_ERROR");
	
	private ErrorCode errorCode;
	private Date exceptionDateTime;
	private String exceptionId;

	public ServiceException(String message, Throwable cause, ErrorCode errorCode){
		super(message, cause);
		this.exceptionId = GenerateUtil.getExceptionID();
		this.errorCode = errorCode;
		this.errorCode.changeMessage(createErrorMessage(this.exceptionId, errorCode));
		this.exceptionDateTime = new Date();
		logException();		
	}
	
	public ErrorCode getErrorCode() {
		return this.errorCode;
	}
	
	public Date getExceptionDateTime() {
		return exceptionDateTime;
	}
	
	public String getExceptionId() {
		return exceptionId;
	}
	
	private String createErrorMessage(String exceptionID, ErrorCode errorCode) {
		if(exceptionID != null && !exceptionID.isBlank() && errorCode != null) {
			String message = errorCode.getMessage().trim();
			if(message.endsWith(".") || message.endsWith(".")) {
				message = message + " PLEASE CONTACT SYSTEM ADMINISTRATOR WITH ID : "  + exceptionID;
			}else {
				message = message + ". PLEASE CONTACT SYSTEM ADMINISTRATOR WITH ID : "  + exceptionID;
			}
			return message;
		}
		return null;
	}

	private void logException() {		
		StringBuilder logBuilder = new StringBuilder();
		if(this.exceptionId != null) {
			logBuilder.append(this.exceptionId);
			logBuilder.append("|");
		}
		if(this.exceptionDateTime != null) {
			logBuilder.append(DateTimeUtil.formatDate(this.exceptionDateTime, DateTimeUtil.dF_dh_yyyyMMdd_dc_HHmmss));
			logBuilder.append("|");
		}
		logBuilder.append(this.getStackTrace()[0].getClassName() + "-" + this.getStackTrace()[0].getMethodName() 
				+ "-" + this.getStackTrace()[0].getLineNumber() 
				+ "::" + this.getCause().getStackTrace()[0].getClassName() + "-" 
				+ this.getCause().getStackTrace()[0].getMethodName() + "-" + this.getCause().getStackTrace()[0].getLineNumber());
		logBuilder.append("|");
		if(this.getCause() != null) {
			logBuilder.append(this.getCause());
			logBuilder.append("|");
		}
		if(this.getMessage() != null && !this.getMessage().isBlank()) {
			logBuilder.append(this.getMessage());
			logBuilder.append("|");
		}
		if(this.errorCode != null) {
			logBuilder.append(this.errorCode.getCode() + "-" + this.errorCode.getMessage());
			logBuilder.append("|");
		}
		logger.error(logBuilder.toString());
	}
}
