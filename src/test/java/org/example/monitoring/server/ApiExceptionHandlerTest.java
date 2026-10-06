package org.example.monitoring.server;

import org.junit.jupiter.api.Test;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void notFound_returnsErrorMessage() {
        // Act
        Map<String, String> body = handler.notFound(new NotFoundException("Server 1 was not found"));

        // Assert
        assertThat(body).containsEntry("error", "Server 1 was not found");
    }

    @Test
    void conflict_returnsErrorMessage() {
        // Act
        Map<String, String> body = handler.conflict(new ConflictException("Hostname already exists"));

        // Assert
        assertThat(body).containsEntry("error", "Hostname already exists");
    }

    @Test
    void invalid_returnsErrorMessage() {
        // Act
        Map<String, String> body = handler.invalid(new InvalidRequestException("hostname is required"));

        // Assert
        assertThat(body).containsEntry("error", "hostname is required");
    }

    @Test
    void validation_withFieldError_returnsFirstFieldMessage() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("serverRequest", "hostname", "must not be blank")));

        // Act
        Map<String, String> body = handler.validation(exception);

        // Assert
        assertThat(body).containsEntry("error", "hostname must not be blank");
    }

    @Test
    void validation_noFieldErrors_returnsGenericMessage() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        // Act
        Map<String, String> body = handler.validation(exception);

        // Assert
        assertThat(body).containsEntry("error", "Request is invalid");
    }
}
