package org.example.monitoring.server;

import org.example.monitoring.aop.BudgetExceededException;
import org.example.monitoring.asyncwork.HealthController;
import org.example.monitoring.asyncwork.MaintenanceController;
import org.example.monitoring.lookup.OperatorNoticeController;
import org.example.monitoring.lookup.RemoteResourceNotFoundException;
import org.example.monitoring.lookup.RemoteServiceUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(assignableTypes = {
        ServerController.class,
        AdminOverviewController.class,
        HealthController.class,
        MaintenanceController.class,
        OperatorNoticeController.class
})
public class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> notFound(NotFoundException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> conflict(ConflictException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(InvalidRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> invalid(InvalidRequestException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(RemoteResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> remoteMissing(RemoteResourceNotFoundException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(RemoteServiceUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, String> remoteUnavailable(RemoteServiceUnavailableException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(BudgetExceededException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, String> budgetExceeded(BudgetExceededException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> validation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getGlobalErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElseGet(() -> exception.getBindingResult().getFieldErrors().stream()
                        .map(error -> error.getField() + " " + error.getDefaultMessage())
                        .findFirst()
                        .orElse("Request is invalid"));
        return Map.of("error", message);
    }
}
