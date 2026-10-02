package uz.aifu.lms.assessment.app.grading;

import java.math.BigDecimal;

/** The outcome of grading one served question against the student's recorded response(s). */
public record GradeResult(BigDecimal pointsAwarded, boolean correct) {}
