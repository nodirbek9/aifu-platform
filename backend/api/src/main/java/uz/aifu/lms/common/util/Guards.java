package uz.aifu.lms.common.util;

import java.util.Collection;
import java.util.Objects;
import uz.aifu.lms.common.error.Exceptions;

/** Small fail-fast helpers for domain constructors and application services. */
public final class Guards {

    private Guards() {}

    public static <T> T required(T value, String name) {
        return Objects.requireNonNull(value, name + " is required");
    }

    public static String requiredText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw Exceptions.validation(name + " must not be blank");
        }
        return value.trim();
    }

    public static void require(boolean condition, String message) {
        if (!condition) {
            throw Exceptions.validation(message);
        }
    }

    public static <T> Collection<T> requireNotEmpty(Collection<T> value, String name) {
        if (value == null || value.isEmpty()) {
            throw Exceptions.validation(name + " must not be empty");
        }
        return value;
    }
}
