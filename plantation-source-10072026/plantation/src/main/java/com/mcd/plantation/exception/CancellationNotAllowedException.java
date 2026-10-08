package com.mcd.plantation.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@ResponseStatus(HttpStatus.CONFLICT)
public class CancellationNotAllowedException extends RuntimeException {
    public CancellationNotAllowedException(String msg) { super(msg); }
}
