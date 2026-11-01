package org.example.monitoring.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.example.monitoring.server.ServerRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ProductionStatusValidatorTest {

    private static Validator validator;

    @BeforeAll
    static void buildValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void prodWithUp_passesConsistentProductionStatus() {
        ServerRequest request = new ServerRequest("host", "10.0.0.1", "PROD", "UP", "");

        Set<ConstraintViolation<ServerRequest>> violations = validator.validate(request);

        assertThat(violations.stream()
                .filter(v -> v.getConstraintDescriptor().getAnnotation() instanceof ConsistentProductionStatus))
                .isEmpty();
    }

    @Test
    void prodWithUnknown_violatesWithExplicitMessage() {
        ServerRequest request = new ServerRequest("host", "10.0.0.1", "PROD", "UNKNOWN", "");

        Set<ConstraintViolation<ServerRequest>> violations = validator.validate(request);

        assertThat(violations).anySatisfy(v -> {
            assertThat(v.getMessage()).containsIgnoringCase("production");
            assertThat(v.getMessage()).containsIgnoringCase("UNKNOWN");
        });
    }
}
