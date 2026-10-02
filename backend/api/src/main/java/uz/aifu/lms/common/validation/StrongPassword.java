package uz.aifu.lms.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Password policy for platform accounts.
 *
 * <p>Length is weighted over character-class gymnastics, per current NIST guidance: a 12-character
 * passphrase beats {@code P@ssw0rd}. A small deny-list catches the handful of values students
 * actually pick.
 */
@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {

    String message() default "{aifu.validation.password}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
