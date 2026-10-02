package uz.aifu.lms.search.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import uz.aifu.lms.search.domain.SearchResult;

/**
 * Full-text search over subjects, courses and lessons (the generated {@code search_vector} columns
 * of their translation tables, ADR-010). A plain {@code JdbcTemplate} query, not a JPA repository:
 * the ranked result spans unrelated tables via {@code UNION ALL}, which maps to no single entity.
 *
 * <p><b>Authorization is baked into the query itself</b> (ADR-004 Layer 3), so nothing the caller
 * may not open can appear, ranked or not:
 *
 * <ul>
 *   <li>only {@code PUBLISHED}, not deleted rows are ever selected;
 *   <li>courses and lessons are those of the caller's own courses — an active enrollment, or a
 *       teaching assignment — unless the caller sees everything (an administrator);
 *   <li>subjects, the catalogue, are for those who see everything: a student or a teacher has no
 *       page to open a bare subject on.
 * </ul>
 *
 * <p>A hit is looked for in every language a text exists in, not only the reader's: most lesson
 * titles exist in Uzbek only, and someone reading the interface in Russian still types the title
 * they see. The reader's language ranks first, and a course ranks above the lessons that merely
 * mention the same word: someone who types "dastur" is looking for Dasturlash before anything else.
 */
@Repository
public class SearchQueryRepository {

    private static final String MAY_OPEN_COURSE =
            """
            (:seesAll
             or exists (select 1 from enrollments e
                        where e.course_id = c.id and e.student_user_id = :userId
                          and e.status = 'ACTIVE')
             or exists (select 1 from course_teachers t
                        where t.course_id = c.id and t.teacher_user_id = :userId))
            """;

    private static final String SQL =
            """
            select result_type, entity_id, course_id, title, snippet, rank from (
                select distinct on (s.id)
                       'SUBJECT' as result_type, s.id as entity_id, null::uuid as course_id,
                       st.title as title,
                       left(coalesce(nullif(st.summary, ''), coalesce(st.description, '')), 240) as snippet,
                       ts_rank(st.search_vector, to_tsquery(fts_config(st.locale), :query))
                           + case when st.locale = :locale then 1 else 0 end as rank
                from subjects s
                join subject_translations st on st.subject_id = s.id
                where :seesAll and s.deleted_at is null and s.status = 'PUBLISHED'
                  and st.search_vector @@ to_tsquery(fts_config(st.locale), :query)
                order by s.id, rank desc
            ) subjects
            union all
            select result_type, entity_id, course_id, title, snippet, rank from (
                select distinct on (c.id)
                       'COURSE' as result_type, c.id as entity_id, c.id as course_id,
                       ct.title as title,
                       left(coalesce(ct.description, ''), 240) as snippet,
                       ts_rank(ct.search_vector, to_tsquery(fts_config(ct.locale), :query))
                           + case when ct.locale = :locale then 1 else 0 end
                           + 0.5 as rank
                from courses c
                join course_translations ct on ct.course_id = c.id
                where c.status = 'PUBLISHED'
                  and ct.search_vector @@ to_tsquery(fts_config(ct.locale), :query)
                  and %s
                order by c.id, rank desc
            ) courses
            union all
            select result_type, entity_id, course_id, title, snippet, rank from (
                select distinct on (l.id)
                       'LESSON' as result_type, l.id as entity_id, c.id as course_id,
                       lt.title as title,
                       left(coalesce(lt.summary, ''), 240) as snippet,
                       ts_rank(lt.search_vector, to_tsquery(fts_config(lt.locale), :query))
                           + case when lt.locale = :locale then 1 else 0 end as rank
                from lessons l
                join course_modules m on m.id = l.module_id and m.deleted_at is null
                join courses c on c.id = m.course_id
                join lesson_translations lt on lt.lesson_id = l.id
                where l.deleted_at is null and l.status = 'PUBLISHED' and c.status = 'PUBLISHED'
                  and lt.search_vector @@ to_tsquery(fts_config(lt.locale), :query)
                  and %s
                order by l.id, rank desc
            ) lessons
            order by rank desc, title
            limit :limit
            """
                    .formatted(MAY_OPEN_COURSE, MAY_OPEN_COURSE);

    private final NamedParameterJdbcTemplate jdbc;

    public SearchQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * @param query a {@code to_tsquery} expression built by the service from the words typed —
     *     never the raw text
     * @param seesAll the caller is not limited to their own courses
     */
    public List<SearchResult> search(
            String locale, String query, UUID userId, boolean seesAll, int limit) {
        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue("locale", locale)
                        .addValue("query", query)
                        .addValue("userId", userId)
                        .addValue("seesAll", seesAll)
                        .addValue("limit", limit);
        return jdbc.query(
                SQL,
                params,
                (rs, rowNum) ->
                        new SearchResult(
                                rs.getString("result_type"),
                                UUID.fromString(rs.getString("entity_id")),
                                rs.getObject("course_id", UUID.class),
                                rs.getString("title"),
                                rs.getString("snippet"),
                                rs.getDouble("rank")));
    }
}
