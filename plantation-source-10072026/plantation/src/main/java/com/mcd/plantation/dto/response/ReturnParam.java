package com.mcd.plantation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class ReturnParam {

    private boolean status;
    private String message;

    @JsonInclude(Include.NON_NULL)
    private String value;

    public ReturnParam() {
    }

    public ReturnParam(boolean status, String message) {
        this.status = status;
        this.message = message;
    }


    public ReturnParam(boolean status, String message, String value) {
        this.status = status;
        this.message = message;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}