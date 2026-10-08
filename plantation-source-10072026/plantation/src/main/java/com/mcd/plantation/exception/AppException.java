package com.mcd.plantation.exception;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.mcd.plantation.pojo.ErrorCode;
import com.mcd.plantation.util.DateTimeUtil;
import com.mcd.plantation.util.GenerateUtil;


@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class AppException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger("APP_ERROR");
	
	private String exceptionId;
	private ErrorCode errorCode;
	private Date exceptionDateTime;
	private String serviceName;
	private String userName;
	private String ipAddr;
	
	public AppException(String message, Throwable cause, 
			ErrorCode errorCode){
		super(message, cause);
		this.exceptionId = GenerateUtil.getExceptionID();
		this.errorCode = errorCode;
		this.errorCode.changeMessage(createErrorMessage(this.exceptionId, errorCode));
		this.exceptionDateTime = new Date();
		//logException();
	}
	
	public AppException(String serviceName, String userName, String ipAddr, 
			String message, Throwable cause, ErrorCode errorCode){
		super(message, cause);
		this.exceptionId = GenerateUtil.getExceptionID();
		this.serviceName = serviceName;
		this.userName = userName;
		this.ipAddr = ipAddr;
		this.errorCode = errorCode;
		this.errorCode.changeMessage(createErrorMessage(this.exceptionId, errorCode));
		this.exceptionDateTime = new Date();
		logException();
	}
	
	public AppException(RuntimeException exception, String serviceName, String userName, String ipAddr) {
		super(exception.getMessage(), exception.getCause());
		if(exception instanceof PersistException) {
			PersistException persistException = (PersistException) exception;
			this.exceptionId = persistException.getExceptionId();
			this.errorCode = persistException.getErrorCode();
			this.exceptionDateTime = persistException.getExceptionDateTime();
		}else {
			ServiceException serviceException = (ServiceException) exception;
			this.exceptionId = serviceException.getExceptionId();
			this.errorCode = serviceException.getErrorCode();
			this.exceptionDateTime = serviceException.getExceptionDateTime();
		}
		this.serviceName = serviceName;
		this.userName = userName;
		this.ipAddr = ipAddr;
		logException();
	}

	public ErrorCode getErrorCode() {
		return this.errorCode;
	}
	
	public Date getExceptionDateTime() {
		return this.exceptionDateTime;
	}
	
	public String getExceptionId() {
		return exceptionId;
	}

	public String getServiceName() {
		return serviceName;
	}

	public String getUserName() {
		return userName;
	}

	public String getIpAddr() {
		return ipAddr;
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
		if(this.exceptionId != null && !this.exceptionId.isBlank()) {
			logBuilder.append(this.exceptionId);
			logBuilder.append("|");
		}
		if(this.serviceName != null && !this.serviceName.isBlank()) {
			logBuilder.append(this.serviceName);
			logBuilder.append("|");
		}
		if(this.userName != null && !this.userName.isBlank()) {
			logBuilder.append(this.userName);
			logBuilder.append("|");
		}
		if(this.ipAddr != null && !this.ipAddr.isBlank()) {
			logBuilder.append(this.ipAddr);
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
		}
		System.out.println(this.getStackTrace()[1].getClassName() + "-" + this.getStackTrace()[1].getMethodName() 
				+ "-" + this.getStackTrace()[1].getLineNumber());
		logger.error(logBuilder.toString());
	}
}
