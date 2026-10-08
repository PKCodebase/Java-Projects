package com.mcd.plantation.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@ResponseStatus(HttpStatus.CONFLICT)
public class SlotFullException extends RuntimeException {
    public SlotFullException(String msg) { super(msg); }
}
