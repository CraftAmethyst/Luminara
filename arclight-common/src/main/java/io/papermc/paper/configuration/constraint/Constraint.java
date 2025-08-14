package io.papermc.paper.configuration.constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Base constraint annotation for Paper configuration validation.
 * This is a simplified implementation for Luminara compatibility.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Constraint {

    /**
     * The constraint message.
     */
    String value() default "";

    /**
     * Base interface for constraint validators.
     */
    interface ConstraintValidator {

        /**
         * Validates the given value.
         */
        boolean validate(Object value);

        /**
         * Gets the constraint message.
         */
        String getMessage();
    }

    /**
     * Factory for creating constraint validators.
     */
    class Factory {

        /**
         * Creates a constraint validator for the given constraint.
         */
        public ConstraintValidator create(Constraint constraint) {
            return new DefaultConstraintValidator(constraint.value());
        }
    }

    /**
     * Default constraint validator implementation.
     */
    class DefaultConstraintValidator implements ConstraintValidator {
        private final String message;

        public DefaultConstraintValidator(String message) {
            this.message = message;
        }

        @Override
        public boolean validate(Object value) {
            // Default implementation always validates
            return true;
        }

        @Override
        public String getMessage() {
            return this.message;
        }
    }
}
