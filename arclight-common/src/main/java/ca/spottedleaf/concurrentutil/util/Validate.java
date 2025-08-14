package ca.spottedleaf.concurrentutil.util;

/**
 * Utility class for validation operations.
 * This class provides validation methods for Paper's concurrent utilities.
 */
public final class Validate {

    private Validate() {
    }

    /**
     * Validates that the specified object is not null.
     *
     * @param object  The object to validate
     * @param message The error message if validation fails
     * @param <T>     The type of the object
     * @return The validated object
     * @throws NullPointerException if the object is null
     */
    public static <T> T notNull(final T object, final String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }
        return object;
    }

    /**
     * Validates that the specified object is not null.
     *
     * @param object The object to validate
     * @param <T>    The type of the object
     * @return The validated object
     * @throws NullPointerException if the object is null
     */
    public static <T> T notNull(final T object) {
        if (object == null) {
            throw new NullPointerException("Object cannot be null");
        }
        return object;
    }

    /**
     * Validates array bounds.
     *
     * @param offset      The offset
     * @param length      The length
     * @param arrayLength The array length
     * @param message     The error message
     * @throws IndexOutOfBoundsException if bounds are invalid
     */
    public static void arrayBounds(final int offset, final int length, final int arrayLength, final String message) {
        if (offset < 0 || length < 0 || offset + length > arrayLength) {
            throw new IndexOutOfBoundsException(message + ": offset=" + offset + ", length=" + length + ", arrayLength=" + arrayLength);
        }
    }

    /**
     * Validates that the specified condition is true.
     *
     * @param condition The condition to validate
     * @param message   The error message if validation fails
     * @throws IllegalArgumentException if the condition is false
     */
    public static void isTrue(final boolean condition, final String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Validates that the specified condition is false.
     *
     * @param condition The condition to validate
     * @param message   The error message if validation fails
     * @throws IllegalArgumentException if the condition is true
     */
    public static void isFalse(final boolean condition, final String message) {
        if (condition) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Validates that the specified value is positive.
     *
     * @param value   The value to validate
     * @param message The error message if validation fails
     * @throws IllegalArgumentException if the value is not positive
     */
    public static void isPositive(final int value, final String message) {
        if (value <= 0) {
            throw new IllegalArgumentException(message + ": " + value);
        }
    }

    /**
     * Validates that the specified value is non-negative.
     *
     * @param value   The value to validate
     * @param message The error message if validation fails
     * @throws IllegalArgumentException if the value is negative
     */
    public static void isNonNegative(final int value, final String message) {
        if (value < 0) {
            throw new IllegalArgumentException(message + ": " + value);
        }
    }

    /**
     * Validates that the specified value is positive.
     *
     * @param value   The value to validate
     * @param message The error message if validation fails
     * @throws IllegalArgumentException if the value is not positive
     */
    public static void isPositive(final long value, final String message) {
        if (value <= 0L) {
            throw new IllegalArgumentException(message + ": " + value);
        }
    }

    /**
     * Validates that the specified value is non-negative.
     *
     * @param value   The value to validate
     * @param message The error message if validation fails
     * @throws IllegalArgumentException if the value is negative
     */
    public static void isNonNegative(final long value, final String message) {
        if (value < 0L) {
            throw new IllegalArgumentException(message + ": " + value);
        }
    }

    /**
     * Validates that the specified string is not null or empty.
     *
     * @param string  The string to validate
     * @param message The error message if validation fails
     * @return The validated string
     * @throws IllegalArgumentException if the string is null or empty
     */
    public static String notEmpty(final String string, final String message) {
        if (string == null || string.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return string;
    }

    /**
     * Validates that the specified collection is not null or empty.
     *
     * @param collection The collection to validate
     * @param message    The error message if validation fails
     * @param <T>        The type of the collection
     * @return The validated collection
     * @throws IllegalArgumentException if the collection is null or empty
     */
    public static <T extends java.util.Collection<?>> T notEmpty(final T collection, final String message) {
        if (collection == null || collection.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return collection;
    }

    /**
     * Validates that the specified array is not null or empty.
     *
     * @param array   The array to validate
     * @param message The error message if validation fails
     * @param <T>     The type of the array
     * @return The validated array
     * @throws IllegalArgumentException if the array is null or empty
     */
    public static <T> T[] notEmpty(final T[] array, final String message) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException(message);
        }
        return array;
    }

    /**
     * Validates that the specified index is within bounds.
     *
     * @param index   The index to validate
     * @param size    The size of the collection/array
     * @param message The error message if validation fails
     * @throws IndexOutOfBoundsException if the index is out of bounds
     */
    public static void indexInBounds(final int index, final int size, final String message) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(message + ": index=" + index + ", size=" + size);
        }
    }

    /**
     * Validates that the specified range is within bounds.
     *
     * @param start   The start index
     * @param end     The end index
     * @param size    The size of the collection/array
     * @param message The error message if validation fails
     * @throws IndexOutOfBoundsException if the range is out of bounds
     */
    public static void rangeInBounds(final int start, final int end, final int size, final String message) {
        if (start < 0 || end < start || end > size) {
            throw new IndexOutOfBoundsException(message + ": start=" + start + ", end=" + end + ", size=" + size);
        }
    }
}
