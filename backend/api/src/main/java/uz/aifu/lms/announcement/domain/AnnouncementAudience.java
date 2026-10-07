package uz.aifu.lms.announcement.domain;

/**
 * Who an announcement is for. The last three reach the whole university and are the study office's.
 */
public enum AnnouncementAudience {
    COURSE,
    GROUP,
    EVERYONE,
    STUDENTS,
    TEACHERS;

    public boolean isWide() {
        return this == EVERYONE || this == STUDENTS || this == TEACHERS;
    }
}
