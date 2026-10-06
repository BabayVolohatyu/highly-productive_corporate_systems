package org.example.monitoring.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = ProductionStatusValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConsistentProductionStatus {

    String message() default "A production server cannot have status UNKNOWN. Use UP, DOWN, or MAINTENANCE.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
