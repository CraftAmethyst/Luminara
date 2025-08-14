package io.papermc.paper.configuration.constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Common constraint annotations for Paper configuration validation.
 * This is a simplified implementation for Luminara compatibility.
 */
public final class Constraints {

    private Constraints() {
    }

    /**
     * Constraint for minimum values.
     */
    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Min {

        /**
         * The minimum value.
         */
        double value();

        /**
         * The constraint message.
         */
        String message() default "Value must be at least {value}";

        /**
         * Factory for creating Min constraint validators.
         */
        class Factory {

            /**
             * Creates a Min constraint validator.
             */
            public MinConstraintValidator create(Min constraint) {
                return new MinConstraintValidator(constraint.value(), constraint.message());
            }
        }

        /**
         * Min constraint validator implementation.
         */
        class MinConstraintValidator implements Constraint.ConstraintValidator {
            private final double minValue;
            private final String message;

            public MinConstraintValidator(double minValue, String message) {
                this.minValue = minValue;
                this.message = message;
            }

            @Override
            public boolean validate(Object value) {
                if (value instanceof Number number) {
                    return number.doubleValue() >= this.minValue;
                }
                return true; // Non-numeric values pass validation
            }

            @Override
            public String getMessage() {
                return this.message.replace("{value}", String.valueOf(this.minValue));
            }
        }
    }

    /**
     * Constraint for maximum values.
     */
    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Max {

        /**
         * The maximum value.
         */
        double value();

        /**
         * The constraint message.
         */
        String message() default "Value must be at most {value}";

        /**
         * Factory for creating Max constraint validators.
         */
        class Factory {

            /**
             * Creates a Max constraint validator.
             */
            public MaxConstraintValidator create(Max constraint) {
                return new MaxConstraintValidator(constraint.value(), constraint.message());
            }
        }

        /**
         * Max constraint validator implementation.
         */
        class MaxConstraintValidator implements Constraint.ConstraintValidator {
            private final double maxValue;
            private final String message;

            public MaxConstraintValidator(double maxValue, String message) {
                this.maxValue = maxValue;
                this.message = message;
            }

            @Override
            public boolean validate(Object value) {
                if (value instanceof Number number) {
                    return number.doubleValue() <= this.maxValue;
                }
                return true; // Non-numeric values pass validation
            }

            @Override
            public String getMessage() {
                return this.message.replace("{value}", String.valueOf(this.maxValue));
            }
        }
    }

    /**
     * Constraint for positive values.
     */
    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Positive {

        /**
         * The constraint message.
         */
        String message() default "Value must be positive";

        /**
         * Factory for creating Positive constraint validators.
         */
        class Factory {

            /**
             * Creates a Positive constraint validator.
             */
            public PositiveConstraintValidator create(Positive constraint) {
                return new PositiveConstraintValidator(constraint.message());
            }
        }

        /**
         * Positive constraint validator implementation.
         */
        class PositiveConstraintValidator implements Constraint.ConstraintValidator {
            private final String message;

            public PositiveConstraintValidator(String message) {
                this.message = message;
            }

            @Override
            public boolean validate(Object value) {
                if (value instanceof Number number) {
                    return number.doubleValue() > 0;
                }
                return true; // Non-numeric values pass validation
            }

            @Override
            public String getMessage() {
                return this.message;
            }
        }
    }

    /**
     * Constraint for non-negative values.
     */
    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface NonNegative {

        /**
         * The constraint message.
         */
        String message() default "Value must be non-negative";

        /**
         * Factory for creating NonNegative constraint validators.
         */
        class Factory {

            /**
             * Creates a NonNegative constraint validator.
             */
            public NonNegativeConstraintValidator create(NonNegative constraint) {
                return new NonNegativeConstraintValidator(constraint.message());
            }
        }

        /**
         * NonNegative constraint validator implementation.
         */
        class NonNegativeConstraintValidator implements Constraint.ConstraintValidator {
            private final String message;

            public NonNegativeConstraintValidator(String message) {
                this.message = message;
            }

            @Override
            public boolean validate(Object value) {
                if (value instanceof Number number) {
                    return number.doubleValue() >= 0;
                }
                return true; // Non-numeric values pass validation
            }

            @Override
            public String getMessage() {
                return this.message;
            }
        }
    }
}
