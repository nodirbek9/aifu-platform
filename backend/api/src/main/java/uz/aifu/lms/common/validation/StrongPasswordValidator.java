package uz.aifu.lms.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;

/** See {@link StrongPassword}. */
public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    /**
     * BCrypt silently ignores bytes past 72; rejecting longer input avoids a surprising truncation.
     */
    private static final int MAX_LENGTH = 72;

    private static final int MIN_LENGTH = 10;

    /** Deliberately short. A real deployment should back this with a leaked-password list. */
    private static final Set<String> DENY_LIST =
            Set.of(
                    "password",
                    "parol",
                    "123456789",
                    "qwertyuiop",
                    "aifu2025",
                    "aifu2026",
                    "student123",
                    "teacher123",
                    "admin12345",
                    "11111111");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // @NotBlank is responsible for presence
        }
        String trimmed = value.trim();
        if (trimmed.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            return reject(
                    context,
                    "Password must be between "
                            + MIN_LENGTH
                            + " and "
                            + MAX_LENGTH
                            + " characters");
        }
        if (DENY_LIST.contains(trimmed.toLowerCase())) {
            return reject(context, "Password is too common");
        }
        long classes =
                (value.chars().anyMatch(Character::isUpperCase) ? 1 : 0)
                        + (value.chars().anyMatch(Character::isLowerCase) ? 1 : 0)
                        + (value.chars().anyMatch(Character::isDigit) ? 1 : 0)
                        + (value.chars().anyMatch(c -> !Character.isLetterOrDigit(c)) ? 1 : 0);
        if (classes < 3) {
            return reject(
                    context,
                    "Password must combine at least three of: lowercase, uppercase, digits, symbols");
        }
        return true;
    }

    private static boolean reject(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        return false;
    }
}
