package org.example.monitoring.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.example.monitoring.server.ServerRequest;

public class ProductionStatusValidator implements ConstraintValidator<ConsistentProductionStatus, ServerRequest> {

    private static final String PROD = "PROD";
    private static final String UNKNOWN = "UNKNOWN";

    @Override
    public boolean isValid(ServerRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }
        String environment = request.environment();
        String status = request.status();
        if (environment == null || status == null || status.isBlank()) {
            return true;
        }
        if (!PROD.equalsIgnoreCase(environment.trim()) || !UNKNOWN.equalsIgnoreCase(status.trim())) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                        "A production server cannot have status UNKNOWN. Use UP, DOWN, or MAINTENANCE.")
                .addConstraintViolation();
        return false;
    }
}
