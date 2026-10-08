package com.mcd.plantation.dto.response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HttpClientResponse {

    private boolean status;
    private String message;
    private int statusCode;
    private String clientInfo;
    private Map<String, List<String>> headers = new HashMap<>();
    private String body;
    private Exception exception;

    public HttpClientResponse(boolean status, String message, String clientInfo) {
        this.status = status;
        this.message = message;
        this.clientInfo = clientInfo;
    }

    public HttpClientResponse(boolean status, String message, Exception exception, String clientInfo) {
        this.status = status;
        this.message = message;
        this.exception = exception;
        this.clientInfo = clientInfo;
    }

    public HttpClientResponse(boolean status, String message, int statusCode, Map<String, List<String>> headers,
                              String body, String clientInfo) {
        this.status = status;
        this.message = message;
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
        this.clientInfo = clientInfo;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Exception getException() {
        return exception;
    }

    public String getClientInfo() {
        return clientInfo;
    }

    public void setClientInfo(String clientInfo) {
        this.clientInfo = clientInfo;
    }
}