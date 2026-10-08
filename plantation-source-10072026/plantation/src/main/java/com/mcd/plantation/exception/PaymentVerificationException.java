package com.mcd.plantation.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@ResponseStatus(HttpStatus.CONFLICT)
public class PaymentVerificationException extends RuntimeException {
    public PaymentVerificationException(String msg) { super(msg); }
}
