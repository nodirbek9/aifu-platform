package uz.aifu.lms.academics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import uz.aifu.lms.common.util.Guards;
import uz.aifu.lms.iam.domain.User;

/**
 * Many-to-many: a teacher teaches many courses, a course may have many teachers (brief §7). This
 * table is the authority behind {@code CourseAccessGuard.requireTeacherOf()} — see {@code
 * CourseTeacherLookupAdapter}.
 */
@Entity
@Table(name = "course_teachers")
public class CourseTeacher {

    @EmbeddedId private CourseTeacherId id;

    @MapsId("courseId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @MapsId("teacherUserId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_user_id")
    private User teacher;

    @Enumerated(EnumType.STRING)
    @Column(name = "teacher_role", nullable = false)
    private TeacherRole teacherRole = TeacherRole.LEAD;

    @CreationTimestamp
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

    @Column(name = "assigned_by", updatable = false)
    private UUID assignedBy;

    protected CourseTeacher() {}

    public CourseTeacher(Course course, User teacher, TeacherRole teacherRole, UUID assignedBy) {
        this.course = Guards.required(course, "course");
        this.teacher = Guards.required(teacher, "teacher");
        this.id = new CourseTeacherId(course.getId(), teacher.getId());
        this.teacherRole = teacherRole == null ? TeacherRole.LEAD : teacherRole;
        this.assignedBy = assignedBy;
    }

    public Course getCourse() {
        return course;
    }

    public User getTeacher() {
        return teacher;
    }

    public TeacherRole getTeacherRole() {
        return teacherRole;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public UUID getAssignedBy() {
        return assignedBy;
    }
}
