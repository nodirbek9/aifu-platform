package uz.aifu.lms.announcement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;
import uz.aifu.lms.common.domain.BaseEntity;
import uz.aifu.lms.common.util.Guards;

/**
 * A message (product spec T-11) to the students of one course or of one student group, or — from
 * the study office — to everyone, to all students or to all teachers. {@link #courseId} is set for
 * a course, {@link #groupId} for a group, neither for the wide audiences.
 */
@Entity
@Table(name = "announcements")
public class Announcement extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "audience", nullable = false, updatable = false)
    private AnnouncementAudience audience;

    @Column(name = "course_id", updatable = false)
    private UUID courseId;

    @Column(name = "group_id", updatable = false)
    private UUID groupId;

    @Column(name = "author_user_id", updatable = false)
    private UUID authorUserId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "body", nullable = false)
    private String body;

    protected Announcement() {}

    public Announcement(
            AnnouncementAudience audience,
            UUID courseId,
            UUID groupId,
            UUID authorUserId,
            String title,
            String body) {
        this.audience = Guards.required(audience, "audience");
        Guards.require(
                (audience == AnnouncementAudience.COURSE) == (courseId != null)
                        && (audience == AnnouncementAudience.GROUP) == (groupId != null),
                "A course announcement names its course, a group one its group, a wide one neither");
        this.courseId = courseId;
        this.groupId = groupId;
        this.authorUserId = Guards.required(authorUserId, "authorUserId");
        this.title = Guards.requiredText(title, "title").trim();
        this.body = Guards.requiredText(body, "body");
    }

    public AnnouncementAudience getAudience() {
        return audience;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public UUID getAuthorUserId() {
        return authorUserId;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }
}
