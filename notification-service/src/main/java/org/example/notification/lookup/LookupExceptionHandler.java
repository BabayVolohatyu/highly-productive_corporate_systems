package org.example.notification.lookup;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(assignableTypes = LookupController.class)
public class LookupExceptionHandler {

    @ExceptionHandler(LookupNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> missing(LookupNotFoundException exception) {
        return Map.of("error", exception.getMessage());
    }
}
