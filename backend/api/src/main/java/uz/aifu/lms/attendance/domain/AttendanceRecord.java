package uz.aifu.lms.attendance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import uz.aifu.lms.common.util.Guards;

/** One student's mark for one class session. No row means the student was not marked. */
@Entity
@Table(name = "attendance_records")
public class AttendanceRecord {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "session_id", nullable = false, updatable = false)
    private UUID sessionId;

    @Column(name = "student_user_id", nullable = false, updatable = false)
    private UUID studentUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AttendanceStatus status;

    @Column(name = "note")
    private String note;

    @Column(name = "marked_by")
    private UUID markedBy;

    @Column(name = "marked_at", nullable = false)
    private Instant markedAt;

    protected AttendanceRecord() {}

    public AttendanceRecord(UUID sessionId, UUID studentUserId) {
        this.sessionId = Guards.required(sessionId, "sessionId");
        this.studentUserId = Guards.required(studentUserId, "studentUserId");
    }

    public void mark(AttendanceStatus status, String note, UUID markedBy, Instant now) {
        this.status = Guards.required(status, "status");
        this.note = (note == null || note.isBlank()) ? null : note.trim();
        this.markedBy = markedBy;
        this.markedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public UUID getStudentUserId() {
        return studentUserId;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public String getNote() {
        return note;
    }

    public UUID getMarkedBy() {
        return markedBy;
    }

    public Instant getMarkedAt() {
        return markedAt;
    }
}
