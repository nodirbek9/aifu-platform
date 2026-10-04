package uz.aifu.lms.execution.persistence;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import uz.aifu.lms.execution.domain.ReviewQueueEntry;
import uz.aifu.lms.execution.domain.ReviewQueueFilter;

/**
 * The submissions of a course as a teacher reviews them: across every coding task of the course,
 * with the task's title, the student, their group and whether feedback was already written. A
 * read-only query spanning several modules' tables (the same reasoning as {@code search}): no
 * entity maps to it and no service of another module has to grow for it.
 */
@Repository
public class ReviewQueueRepository {

    private static final String FROM =
            """
            from submissions s
            join coding_tasks ct on ct.id = s.coding_task_id
            join enrollments e on e.id = s.enrollment_id
            join users u on u.id = e.student_user_id
            left join student_profiles p on p.user_id = u.id
            where ct.course_id = :courseId
              and (cast(:taskId as uuid) is null or s.coding_task_id = cast(:taskId as uuid))
              and (cast(:verdict as text) is null or s.verdict = cast(:verdict as text))
              and (cast(:groupCode as text) is null or p.group_code = cast(:groupCode as text))
              and (not :needsFeedback or not exists (
                    select 1 from feedback f where f.target_type = 'SUBMISSION' and f.target_id = s.id))
              and (not :latestOnly or s.attempt_number = (
                    select max(s2.attempt_number) from submissions s2
                    where s2.enrollment_id = s.enrollment_id and s2.coding_task_id = s.coding_task_id))
            """;

    private static final String SELECT =
            """
            select s.id, s.coding_task_id,
                   coalesce((select t.title from coding_task_translations t
                             where t.coding_task_id = ct.id
                             order by (t.locale = :locale) desc, (t.locale = 'uz') desc limit 1), ct.code) as task_title,
                   u.id as student_id, u.full_name, p.group_code, s.attempt_number, s.language_code,
                   s.status, s.verdict, s.score, s.max_score, s.submitted_at,
                   exists (select 1 from feedback f
                           where f.target_type = 'SUBMISSION' and f.target_id = s.id) as has_feedback,
                   coalesce(
                       (select a.due_at from activities a where a.id = s.activity_id),
                       (select min(a2.due_at) from activities a2
                        where a2.reference_id = ct.id and a2.activity_type = 'CODING_TASK'
                          and a2.course_id = ct.course_id)) as due_at
            """
                    + FROM
                    + """
                    order by s.submitted_at desc
                    limit :limit offset :offset
                    """;

    private static final String COUNT = "select count(*) " + FROM;

    private final NamedParameterJdbcTemplate jdbc;

    public ReviewQueueRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<ReviewQueueEntry> find(
            UUID courseId, ReviewQueueFilter filter, String locale, int limit, long offset) {
        return jdbc.query(
                SELECT,
                params(courseId, filter)
                        .addValue("locale", locale)
                        .addValue("limit", limit)
                        .addValue("offset", offset),
                (rs, rowNum) ->
                        new ReviewQueueEntry(
                                rs.getObject("id", UUID.class),
                                rs.getObject("coding_task_id", UUID.class),
                                rs.getString("task_title"),
                                rs.getObject("student_id", UUID.class),
                                rs.getString("full_name"),
                                rs.getString("group_code"),
                                rs.getInt("attempt_number"),
                                rs.getString("language_code"),
                                rs.getString("status"),
                                rs.getString("verdict"),
                                rs.getBigDecimal("score"),
                                rs.getBigDecimal("max_score"),
                                rs.getTimestamp("submitted_at") == null
                                        ? null
                                        : rs.getTimestamp("submitted_at").toInstant(),
                                rs.getBoolean("has_feedback"),
                                dueAt(rs),
                                isLate(rs)));
    }

    /**
     * The deadline the submission answers: the activity it was made for, else the earliest activity
     * of the course that sets the task. Late work is accepted (the owner's decision: the teacher
     * decides), it is only marked.
     */
    private static Instant dueAt(java.sql.ResultSet rs) throws java.sql.SQLException {
        java.sql.Timestamp due = rs.getTimestamp("due_at");
        return due == null ? null : due.toInstant();
    }

    private static boolean isLate(java.sql.ResultSet rs) throws java.sql.SQLException {
        Instant due = dueAt(rs);
        java.sql.Timestamp submitted = rs.getTimestamp("submitted_at");
        return due != null && submitted != null && submitted.toInstant().isAfter(due);
    }

    public long count(UUID courseId, ReviewQueueFilter filter) {
        Long total = jdbc.queryForObject(COUNT, params(courseId, filter), Long.class);
        return total == null ? 0 : total;
    }

    private static MapSqlParameterSource params(UUID courseId, ReviewQueueFilter filter) {
        return new MapSqlParameterSource()
                .addValue("courseId", courseId)
                .addValue("taskId", filter.taskId())
                .addValue("verdict", filter.verdict())
                .addValue("groupCode", filter.groupCode())
                .addValue("needsFeedback", filter.needsFeedback())
                .addValue("latestOnly", filter.latestOnly());
    }
}
