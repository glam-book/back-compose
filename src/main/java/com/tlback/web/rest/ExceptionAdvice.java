package com.tlback.web.rest;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.tlback.core.abac.exception.ForbiddenException;
import com.tlback.core.abac.exception.NotFoundException;
import com.tlback.core.abac.exception.RightsException;
import com.tlback.core.service.exception.RecordPendingException;

import io.swagger.v3.oas.annotations.Hidden;

@RestControllerAdvice
@Hidden
public class ExceptionAdvice {

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleForbidden(ForbiddenException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(RightsException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handlePermissionsException(RightsException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NotFoundException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(RecordPendingException.class)
    public ResponseEntity<ErrorResponse> handleRecordPednignExceptions(RecordPendingException ex) {
        var response = ErrorResponse.create(ex, HttpStatus.CONFLICT,
                Objects.toString(ex.getLocalizedMessage()));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}