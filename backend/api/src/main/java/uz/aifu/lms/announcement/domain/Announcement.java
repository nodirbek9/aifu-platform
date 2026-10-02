package uz.aifu.lms.announcement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import uz.aifu.lms.common.domain.BaseEntity;
import uz.aifu.lms.common.util.Guards;

/**
 * A message to the students of one course or of one student group (product spec T-11). Exactly one
 * of {@link #courseId} and {@link #groupId} is set.
 */
@Entity
@Table(name = "announcements")
public class Announcement extends BaseEntity {

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

    public Announcement(UUID courseId, UUID groupId, UUID authorUserId, String title, String body) {
        Guards.require(
                (courseId == null) != (groupId == null),
                "An announcement is for a course or for a group, not both");
        this.courseId = courseId;
        this.groupId = groupId;
        this.authorUserId = Guards.required(authorUserId, "authorUserId");
        this.title = Guards.requiredText(title, "title").trim();
        this.body = Guards.requiredText(body, "body");
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
