package uz.aifu.lms.scheduling.app;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.academics.app.AcademicCalendarService;
import uz.aifu.lms.academics.app.CourseDirectory;
import uz.aifu.lms.academics.domain.SessionType;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.iam.app.user.StudentDirectory;
import uz.aifu.lms.scheduling.api.dto.SchedulingDtos.LoadItemRequest;
import uz.aifu.lms.scheduling.domain.StudentGroup;
import uz.aifu.lms.scheduling.domain.TeachingLoadItem;
import uz.aifu.lms.scheduling.persistence.StudentGroupRepository;
import uz.aifu.lms.scheduling.persistence.TeachingLoadItemRepository;

/**
 * The planned teaching load ("yuklama") of a term: how many hours each group must receive per
 * course and session type, and from whom. The client parses the department's Excel sheet and sends
 * the rows; this service only stores them.
 */
@Service
public class TeachingLoadService {

    private final TeachingLoadItemRepository items;
    private final StudentGroupRepository groups;
    private final AcademicCalendarService calendar;
    private final CourseDirectory courses;
    private final StudentDirectory people;

    public TeachingLoadService(
            TeachingLoadItemRepository items,
            StudentGroupRepository groups,
            AcademicCalendarService calendar,
            CourseDirectory courses,
            StudentDirectory people) {
        this.items = items;
        this.groups = groups;
        this.calendar = calendar;
        this.courses = courses;
        this.people = people;
    }

    /**
     * Creates or updates one line per (course, group, session type). Sending the same sheet twice
     * changes nothing.
     *
     * @return how many lines were written
     */
    @Transactional
    public int upsert(UUID termId, List<LoadItemRequest> requests) {
        calendar.requireTerm(termId);
        for (LoadItemRequest request : requests) {
            if (request.sessionType() == SessionType.INDEPENDENT_STUDY
                    || request.sessionType() == SessionType.COURSE_WORK) {
                throw Exceptions.rule(
                        ErrorCode.VALIDATION_FAILED,
                        "Only lecture, seminar, practical and laboratory hours are scheduled");
            }
            if (!courses.exists(request.courseId())) {
                throw Exceptions.notFound("Course", request.courseId());
            }
            if (request.teacherUserId() != null && !people.userExists(request.teacherUserId())) {
                throw Exceptions.notFound("User", request.teacherUserId());
            }
            StudentGroup group =
                    groups.findById(request.groupId())
                            .orElseThrow(
                                    () -> Exceptions.notFound("StudentGroup", request.groupId()));
            TeachingLoadItem item =
                    items.findByTermIdAndCourseIdAndGroup_IdAndSessionType(
                                    termId,
                                    request.courseId(),
                                    request.groupId(),
                                    request.sessionType())
                            .orElseGet(
                                    () ->
                                            new TeachingLoadItem(
                                                    termId,
                                                    request.courseId(),
                                                    group,
                                                    request.sessionType()));
            item.plan(request.teacherUserId(), request.plannedHours());
            items.save(item);
        }
        return requests.size();
    }

    @Transactional
    public void delete(UUID itemId) {
        TeachingLoadItem item =
                items.findById(itemId)
                        .orElseThrow(() -> Exceptions.notFound("TeachingLoadItem", itemId));
        items.delete(item);
    }
}
