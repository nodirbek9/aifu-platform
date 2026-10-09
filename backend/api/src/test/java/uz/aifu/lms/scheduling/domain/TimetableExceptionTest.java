package uz.aifu.lms.scheduling.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import uz.aifu.lms.common.error.DomainException;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.ExceptionRequest;

class TimetableExceptionTest {

    private final TimetableEntry entry = new TimetableEntry();
    private final LocalDate date = LocalDate.of(2026, 10, 10);
    private final UUID createdBy = UUID.randomUUID();
    private final PairSlot pairSlot = new PairSlot();
    private final Room room = new Room();

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validAddException() {
        UUID teacherId = UUID.randomUUID();
        TimetableException ex =
                new TimetableException(
                        entry,
                        date,
                        ExceptionKind.ADD,
                        null,
                        pairSlot,
                        room,
                        teacherId,
                        "  Extra session for midterm prep  ",
                        createdBy);

        assertThat(ex.getEntry()).isSameAs(entry);
        assertThat(ex.getOnDate()).isEqualTo(date);
        assertThat(ex.getKind()).isEqualTo(ExceptionKind.ADD);
        assertThat(ex.getNewDate()).isNull();
        assertThat(ex.getNewPairSlot()).isSameAs(pairSlot);
        assertThat(ex.getNewRoom()).isSameAs(room);
        assertThat(ex.getNewTeacherUserId()).isEqualTo(teacherId);
        assertThat(ex.getReason()).isEqualTo("Extra session for midterm prep");
    }

    @Test
    void validAddException_withoutOptionalOverrides() {
        TimetableException ex =
                new TimetableException(
                        entry,
                        date,
                        ExceptionKind.ADD,
                        null,
                        pairSlot,
                        null,
                        null,
                        "Required makeup session",
                        createdBy);

        assertThat(ex.getKind()).isEqualTo(ExceptionKind.ADD);
        assertThat(ex.getNewDate()).isNull();
        assertThat(ex.getNewPairSlot()).isSameAs(pairSlot);
        assertThat(ex.getNewRoom()).isNull();
        assertThat(ex.getNewTeacherUserId()).isNull();
        assertThat(ex.getReason()).isEqualTo("Required makeup session");
    }

    @Test
    void addMissingPairSlot() {
        assertThatThrownBy(
                        () ->
                                new TimetableException(
                                        entry,
                                        date,
                                        ExceptionKind.ADD,
                                        null,
                                        null,
                                        room,
                                        null,
                                        "Reason",
                                        createdBy))
                .isInstanceOf(DomainException.class)
                .extracting(e -> ((DomainException) e).errorCode())
                .isEqualTo(ErrorCode.VALIDATION_FAILED);
    }

    @Test
    void addMissingReason() {
        assertThatThrownBy(
                        () ->
                                new TimetableException(
                                        entry,
                                        date,
                                        ExceptionKind.ADD,
                                        null,
                                        pairSlot,
                                        null,
                                        null,
                                        null,
                                        createdBy))
                .isInstanceOf(DomainException.class)
                .extracting(e -> ((DomainException) e).errorCode())
                .isEqualTo(ErrorCode.VALIDATION_FAILED);

        assertThatThrownBy(
                        () ->
                                new TimetableException(
                                        entry,
                                        date,
                                        ExceptionKind.ADD,
                                        null,
                                        pairSlot,
                                        null,
                                        null,
                                        "   ",
                                        createdBy))
                .isInstanceOf(DomainException.class)
                .extracting(e -> ((DomainException) e).errorCode())
                .isEqualTo(ErrorCode.VALIDATION_FAILED);
    }

    @Test
    void addReasonTooLong() {
        String longReason = "a".repeat(301);
        assertThatThrownBy(
                        () ->
                                new TimetableException(
                                        entry,
                                        date,
                                        ExceptionKind.ADD,
                                        null,
                                        pairSlot,
                                        null,
                                        null,
                                        longReason,
                                        createdBy))
                .isInstanceOf(DomainException.class)
                .extracting(e -> ((DomainException) e).errorCode())
                .isEqualTo(ErrorCode.VALIDATION_FAILED);
    }

    @Test
    void addInvalidExtraDate() {
        assertThatThrownBy(
                        () ->
                                new TimetableException(
                                        entry,
                                        date,
                                        ExceptionKind.ADD,
                                        date.plusDays(1),
                                        pairSlot,
                                        null,
                                        null,
                                        "Reason",
                                        createdBy))
                .isInstanceOf(DomainException.class)
                .extracting(e -> ((DomainException) e).errorCode())
                .isEqualTo(ErrorCode.VALIDATION_FAILED);
    }

    @Test
    void oldKindValidationPreserved() {
        // MOVED requires newDate and newPairSlot
        assertThatThrownBy(
                        () ->
                                new TimetableException(
                                        entry,
                                        date,
                                        ExceptionKind.MOVED,
                                        null,
                                        pairSlot,
                                        null,
                                        null,
                                        null,
                                        createdBy))
                .isInstanceOf(DomainException.class)
                .extracting(e -> ((DomainException) e).errorCode())
                .isEqualTo(ErrorCode.VALIDATION_FAILED);

        assertThatThrownBy(
                        () ->
                                new TimetableException(
                                        entry,
                                        date,
                                        ExceptionKind.MOVED,
                                        date.plusDays(2),
                                        null,
                                        null,
                                        null,
                                        null,
                                        createdBy))
                .isInstanceOf(DomainException.class)
                .extracting(e -> ((DomainException) e).errorCode())
                .isEqualTo(ErrorCode.VALIDATION_FAILED);

        // REPLACED requires replacement teacher or room
        assertThatThrownBy(
                        () ->
                                new TimetableException(
                                        entry,
                                        date,
                                        ExceptionKind.REPLACED,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        createdBy))
                .isInstanceOf(DomainException.class)
                .extracting(e -> ((DomainException) e).errorCode())
                .isEqualTo(ErrorCode.VALIDATION_FAILED);

        // CANCELLED is valid without newDate, slot, room or teacher
        TimetableException cancelled =
                new TimetableException(
                        entry,
                        date,
                        ExceptionKind.CANCELLED,
                        null,
                        null,
                        null,
                        null,
                        "Teacher ill",
                        createdBy);
        assertThat(cancelled.getKind()).isEqualTo(ExceptionKind.CANCELLED);
        assertThat(cancelled.getNewDate()).isNull();
        assertThat(cancelled.getNewPairSlot()).isNull();
        assertThat(cancelled.getNewRoom()).isNull();
        assertThat(cancelled.getNewTeacherUserId()).isNull();
    }

    // ------------------------------------------------------------------ Bean Validation on DTO

    @Test
    void exceptionRequest_validAdd() {
        ExceptionRequest request =
                new ExceptionRequest(
                        date,
                        ExceptionKind.ADD,
                        null,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Added review lecture");
        Set<ConstraintViolation<ExceptionRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void exceptionRequest_addMissingPairSlot() {
        ExceptionRequest request =
                new ExceptionRequest(
                        date, ExceptionKind.ADD, null, null, null, null, "Added review lecture");
        Set<ConstraintViolation<ExceptionRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("requires newPairSlotId");
    }

    @Test
    void exceptionRequest_addWithNewDate() {
        ExceptionRequest request =
                new ExceptionRequest(
                        date,
                        ExceptionKind.ADD,
                        date.plusDays(1),
                        UUID.randomUUID(),
                        null,
                        null,
                        "Added review lecture");
        Set<ConstraintViolation<ExceptionRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("no newDate");
    }

    @Test
    void exceptionRequest_addMissingOrBlankReason() {
        ExceptionRequest nullReason =
                new ExceptionRequest(
                        date, ExceptionKind.ADD, null, UUID.randomUUID(), null, null, null);
        Set<ConstraintViolation<ExceptionRequest>> nullViolations = validator.validate(nullReason);
        assertThat(nullViolations).hasSize(1);

        ExceptionRequest blankReason =
                new ExceptionRequest(
                        date, ExceptionKind.ADD, null, UUID.randomUUID(), null, null, "   ");
        Set<ConstraintViolation<ExceptionRequest>> blankViolations =
                validator.validate(blankReason);
        assertThat(blankViolations).hasSize(1);
    }

    @Test
    void exceptionRequest_addReasonTooLong() {
        ExceptionRequest request =
                new ExceptionRequest(
                        date,
                        ExceptionKind.ADD,
                        null,
                        UUID.randomUUID(),
                        null,
                        null,
                        "a".repeat(301));
        Set<ConstraintViolation<ExceptionRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void exceptionRequest_nonAddUnaffected() {
        ExceptionRequest cancelled =
                new ExceptionRequest(date, ExceptionKind.CANCELLED, null, null, null, null, null);
        assertThat(validator.validate(cancelled)).isEmpty();

        ExceptionRequest moved =
                new ExceptionRequest(
                        date,
                        ExceptionKind.MOVED,
                        date.plusDays(3),
                        UUID.randomUUID(),
                        null,
                        null,
                        "Shifted pair");
        assertThat(validator.validate(moved)).isEmpty();
    }
}
