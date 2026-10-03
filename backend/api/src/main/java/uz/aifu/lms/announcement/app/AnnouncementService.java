package uz.aifu.lms.announcement.app;

import java.time.Clock;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.academics.app.CourseDirectory;
import uz.aifu.lms.academics.app.CourseDirectory.CourseLabel;
import uz.aifu.lms.announcement.api.dto.AnnouncementDtos.AnnouncementView;
import uz.aifu.lms.announcement.api.dto.AnnouncementDtos.CreateAnnouncementRequest;
import uz.aifu.lms.announcement.domain.Announcement;
import uz.aifu.lms.announcement.persistence.AnnouncementRepository;
import uz.aifu.lms.common.content.ContentSanitizer;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.events.NotificationRequestedEvent;
import uz.aifu.lms.common.events.NotificationsWithdrawnEvent;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.common.security.AuthenticatedUser;
import uz.aifu.lms.enrollment.app.EnrollmentService;
import uz.aifu.lms.enrollment.domain.Enrollment;
import uz.aifu.lms.enrollment.domain.EnrollmentStatus;
import uz.aifu.lms.iam.app.access.AccessGuard;
import uz.aifu.lms.iam.app.access.Perm;
import uz.aifu.lms.iam.app.user.StudentDirectory;
import uz.aifu.lms.iam.app.user.StudentDirectory.StudentRef;
import uz.aifu.lms.scheduling.app.SchedulingReferenceService;
import uz.aifu.lms.scheduling.domain.StudentGroup;

/**
 * Announcements (product spec T-11): a teacher or the study office writes to the students of a
 * course or of a group; each of them gets a notification and finds the message in their list.
 */
@Service
public class AnnouncementService {

    /** Stands in for "no ids" in an IN list. */
    private static final UUID NOTHING = new UUID(0L, 0L);

    private final AnnouncementRepository announcements;
    private final AccessGuard accessGuard;
    private final EnrollmentService enrollments;
    private final CourseDirectory courses;
    private final SchedulingReferenceService groups;
    private final StudentDirectory people;
    private final ContentSanitizer sanitizer;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    public AnnouncementService(
            AnnouncementRepository announcements,
            AccessGuard accessGuard,
            EnrollmentService enrollments,
            CourseDirectory courses,
            SchedulingReferenceService groups,
            StudentDirectory people,
            ContentSanitizer sanitizer,
            ApplicationEventPublisher events,
            Clock clock) {
        this.announcements = announcements;
        this.accessGuard = accessGuard;
        this.enrollments = enrollments;
        this.courses = courses;
        this.groups = groups;
        this.people = people;
        this.sanitizer = sanitizer;
        this.events = events;
        this.clock = clock;
    }

    @Transactional
    public AnnouncementView post(CreateAnnouncementRequest request, LocaleCode locale) {
        if ((request.courseId() == null) == (request.groupId() == null)) {
            throw Exceptions.validation("Give exactly one of courseId and groupId");
        }
        AuthenticatedUser author = accessGuard.caller();
        Set<UUID> recipients = new HashSet<>();
        if (request.courseId() != null) {
            accessGuard.requireCourseEditor(request.courseId());
            enrollments
                    .listByCourse(
                            request.courseId(),
                            List.of(EnrollmentStatus.ACTIVE),
                            Pageable.unpaged())
                    .forEach(e -> recipients.add(e.getStudent().getId()));
        } else {
            StudentGroup group = groups.requireGroup(request.groupId());
            requireMayAddress(group, author);
            people.studentsOfGroup(group.getCode()).stream()
                    .map(StudentRef::userId)
                    .forEach(recipients::add);
        }
        recipients.remove(author.id());

        Announcement saved =
                announcements.save(
                        new Announcement(
                                request.courseId(),
                                request.groupId(),
                                author.id(),
                                request.title(),
                                sanitizer.sanitizeRichText(request.body())));
        Map<String, Object> payload =
                request.courseId() != null
                        ? Map.of(
                                "announcementId",
                                saved.getId().toString(),
                                "title",
                                saved.getTitle(),
                                "courseId",
                                request.courseId().toString())
                        : Map.of(
                                "announcementId",
                                saved.getId().toString(),
                                "title",
                                saved.getTitle(),
                                "groupId",
                                request.groupId().toString());
        for (UUID recipient : recipients) {
            events.publishEvent(
                    new NotificationRequestedEvent(
                            recipient,
                            "ANNOUNCEMENT",
                            payload,
                            "announcement:" + saved.getId(),
                            clock.instant()));
        }
        return toViews(List.of(saved), author, locale).get(0);
    }

    /**
     * What the caller should see, newest first: a student gets the announcements of their current
     * courses and of their group; a teacher those of the courses they teach and the ones they
     * wrote; whoever manages groups (the study office) gets all of them.
     */
    @Transactional(readOnly = true)
    public Page<AnnouncementView> mine(Pageable pageable, LocaleCode locale) {
        AuthenticatedUser user = accessGuard.caller();
        Page<Announcement> page;
        if (user.has(Perm.GROUP_MANAGE)) {
            page = announcements.findAllByOrderByCreatedAtDesc(unsorted(pageable));
        } else {
            Set<UUID> courseIds = new HashSet<>(courses.courseIdsOfTeacher(user.id()));
            for (Enrollment e : enrollments.listByStudent(user.id(), Pageable.unpaged())) {
                if (!e.getStatus().isTerminal()) {
                    courseIds.add(e.getCourse().getId());
                }
            }
            courseIds.add(NOTHING);
            Set<UUID> groupIds = new HashSet<>();
            groups.groupIdOfStudent(user.id()).ifPresent(groupIds::add);
            groupIds.add(NOTHING);
            page = announcements.findVisible(courseIds, groupIds, user.id(), unsorted(pageable));
        }
        return toPage(page, user, locale);
    }

    /** The announcements of one course, for anyone who can open the course. */
    @Transactional(readOnly = true)
    public Page<AnnouncementView> ofCourse(UUID courseId, Pageable pageable, LocaleCode locale) {
        accessGuard.requireCourseViewer(courseId);
        Page<Announcement> page =
                announcements.findByCourseIdOrderByCreatedAtDesc(courseId, unsorted(pageable));
        return toPage(page, accessGuard.caller(), locale);
    }

    /** Its author may delete an announcement, and so may whoever manages groups. */
    @Transactional
    public void delete(UUID id) {
        Announcement announcement =
                announcements
                        .findById(id)
                        .orElseThrow(() -> Exceptions.notFound("Announcement", id));
        if (!mayDelete(announcement, accessGuard.caller())) {
            throw Exceptions.notFound("Announcement", id);
        }
        announcements.delete(announcement);
        events.publishEvent(
                new NotificationsWithdrawnEvent("announcement:" + announcement.getId()));
    }

    /**
     * A group is addressed by the study office (GROUP_MANAGE) or by the group's own curator. A
     * teacher who is neither writes to the students through their course instead.
     */
    private static void requireMayAddress(StudentGroup group, AuthenticatedUser author) {
        if (!author.has(Perm.GROUP_MANAGE) && !author.id().equals(group.getCuratorUserId())) {
            throw Exceptions.forbidden(
                    "Only the study office or the group's curator can write to a whole group");
        }
    }

    private static boolean mayDelete(Announcement announcement, AuthenticatedUser user) {
        return user.id().equals(announcement.getAuthorUserId()) || user.has(Perm.GROUP_MANAGE);
    }

    /** The order is fixed (newest first); a client-chosen sort is ignored. */
    private static Pageable unsorted(Pageable pageable) {
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
    }

    private Page<AnnouncementView> toPage(
            Page<Announcement> page, AuthenticatedUser viewer, LocaleCode locale) {
        Map<UUID, AnnouncementView> views =
                toViews(page.getContent(), viewer, locale).stream()
                        .collect(Collectors.toMap(AnnouncementView::id, v -> v));
        return page.map(a -> views.get(a.getId()));
    }

    private List<AnnouncementView> toViews(
            List<Announcement> found, AuthenticatedUser viewer, LocaleCode locale) {
        Map<UUID, CourseLabel> courseLabels =
                courses.labels(
                        found.stream()
                                .map(Announcement::getCourseId)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toSet()),
                        locale);
        Map<UUID, String> authors =
                people.fullNames(
                        found.stream()
                                .map(Announcement::getAuthorUserId)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toSet()));
        return found.stream()
                .map(
                        a -> {
                            CourseLabel course =
                                    a.getCourseId() == null
                                            ? null
                                            : courseLabels.get(a.getCourseId());
                            return new AnnouncementView(
                                    a.getId(),
                                    a.getCourseId(),
                                    course == null ? null : course.subjectCode(),
                                    course == null ? null : course.title(),
                                    a.getGroupId(),
                                    a.getGroupId() == null
                                            ? null
                                            : groups.requireGroup(a.getGroupId()).getCode(),
                                    a.getAuthorUserId(),
                                    a.getAuthorUserId() == null
                                            ? null
                                            : authors.get(a.getAuthorUserId()),
                                    a.getTitle(),
                                    a.getBody(),
                                    a.getCreatedAt(),
                                    mayDelete(a, viewer));
                        })
                .toList();
    }
}
