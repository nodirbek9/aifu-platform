package uz.aifu.lms.scheduling.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import uz.aifu.lms.academics.domain.SessionType;
import uz.aifu.lms.common.domain.BaseEntity;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.util.Guards;

/** One line of the department's "yuklama": hours a group must receive for a course in a term. */
@Entity
@Table(name = "teaching_load_items")
public class TeachingLoadItem extends BaseEntity {

    @Column(name = "term_id", nullable = false, updatable = false)
    private UUID termId;

    @Column(name = "course_id", nullable = false, updatable = false)
    private UUID courseId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false, updatable = false)
    private StudentGroup group;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", nullable = false, updatable = false)
    private SessionType sessionType;

    @Column(name = "teacher_user_id")
    private UUID teacherUserId;

    @Column(name = "planned_hours", nullable = false)
    private int plannedHours;

    protected TeachingLoadItem() {}

    public TeachingLoadItem(
            UUID termId, UUID courseId, StudentGroup group, SessionType sessionType) {
        this.termId = Guards.required(termId, "termId");
        this.courseId = Guards.required(courseId, "courseId");
        this.group = Guards.required(group, "group");
        this.sessionType = Guards.required(sessionType, "sessionType");
    }

    public void plan(UUID teacherUserId, int plannedHours) {
        if (plannedHours < 0) {
            throw Exceptions.rule(ErrorCode.VALIDATION_FAILED, "plannedHours must not be negative");
        }
        this.teacherUserId = teacherUserId;
        this.plannedHours = plannedHours;
    }

    public UUID getTermId() {
        return termId;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public StudentGroup getGroup() {
        return group;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public UUID getTeacherUserId() {
        return teacherUserId;
    }

    public int getPlannedHours() {
        return plannedHours;
    }
}
