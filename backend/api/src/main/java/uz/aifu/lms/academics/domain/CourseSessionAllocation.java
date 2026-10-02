package uz.aifu.lms.academics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.util.Guards;

/**
 * The institutional hour budget per session type: 30/30/90 (IAKTT1105), 60/60/210 (DAS1110). {@link
 * uz.aifu.lms.academics.app.HourBudgetValidator} reconciles this against lesson hours once
 * curriculum (Phase 5) exists.
 */
@Entity
@Table(name = "course_session_allocations")
public class CourseSessionAllocation {

    @EmbeddedId private CourseSessionAllocationId id;

    @MapsId("courseId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "allocated_hours", nullable = false)
    private int allocatedHours;

    protected CourseSessionAllocation() {}

    public CourseSessionAllocation(Course course, SessionType sessionType, int allocatedHours) {
        this.course = Guards.required(course, "course");
        this.id =
                new CourseSessionAllocationId(
                        course.getId(), Guards.required(sessionType, "sessionType"));
        setAllocatedHours(allocatedHours);
    }

    public void setAllocatedHours(int allocatedHours) {
        if (allocatedHours < 0) {
            throw Exceptions.rule(
                    ErrorCode.VALIDATION_FAILED, "allocatedHours must not be negative");
        }
        this.allocatedHours = allocatedHours;
    }

    public Course getCourse() {
        return course;
    }

    public SessionType getSessionType() {
        return id.getSessionType();
    }

    public int getAllocatedHours() {
        return allocatedHours;
    }
}
