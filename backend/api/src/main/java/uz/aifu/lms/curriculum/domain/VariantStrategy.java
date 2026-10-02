package uz.aifu.lms.curriculum.domain;

/**
 * DAS1110 requires "variativ" independent tasks: per-student variant assignment. Assignment is
 * deterministic: {@code hash(student_user_id, activity_id) % variant_count} (V007 migration).
 */
public enum VariantStrategy {
    NONE,
    DETERMINISTIC_BY_STUDENT,
    RANDOM
}
