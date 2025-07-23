package com.marlonleoner.musicando.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.marlonleoner.musicando.domain.Error;
import com.marlonleoner.musicando.domain.exception.BaseException;

@RestControllerAdvice
public class ExceptionHandlers {

    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public Error handleGenericException(Exception ex) {
        return new Error(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
    }

    @ExceptionHandler(BaseException.class)
    @ResponseBody
    public ResponseEntity<Error> handleException(BaseException ex) {
        Error error = new Error(ex.getCode(), ex.getMessage());
        HttpStatusCode statusCode = HttpStatusCode.valueOf(ex.getCode());

        return new ResponseEntity<>(error, statusCode);
    }
}