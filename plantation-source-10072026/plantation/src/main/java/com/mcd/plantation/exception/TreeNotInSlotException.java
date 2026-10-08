package com.mcd.plantation.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@ResponseStatus(HttpStatus.CONFLICT)
public class TreeNotInSlotException extends RuntimeException {
    public TreeNotInSlotException(String msg) { super(msg); }
}
