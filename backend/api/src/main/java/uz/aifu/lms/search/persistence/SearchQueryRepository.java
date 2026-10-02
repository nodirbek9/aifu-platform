package uz.aifu.lms.search.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import uz.aifu.lms.search.domain.SearchResult;

/**
 * Full-text search over subjects + courses (V004/V005's generated {@code search_vector} columns,
 * ADR-010). A plain {@code JdbcTemplate} query, not a JPA repository: the ranked result spans two
 * unrelated tables via {@code UNION ALL}, which does not map to any single entity — the same
 * reasoning {@code analytics} uses read-only view entities for, except here there is no view to
 * back a {@code @Entity} because a rank-ordered cross-table union is inherently query-shaped, not
 * table-shaped.
 *
 * <p><b>Authorization is baked into the query itself</b> (ADR-004 Layer 3): only {@code status =
 * 'PUBLISHED'} rows are ever selected, so an unauthorized/unpublished subject or course can never
 * appear in a result, ranked or not — there is no separate "hide it after the fact" step to forget.
 */
@Repository
public class SearchQueryRepository {

    private static final String SQL =
            """
            select 'SUBJECT' as result_type, s.id as entity_id, st.title as title,
                   left(coalesce(nullif(st.summary, ''), coalesce(st.description, '')), 240) as snippet,
                   ts_rank(st.search_vector, q) as rank
            from subjects s
            join subject_translations st on st.subject_id = s.id and st.locale = :locale
            cross join websearch_to_tsquery(fts_config(:locale), :queryText) q
            where s.deleted_at is null and s.status = 'PUBLISHED' and st.search_vector @@ q
            union all
            select 'COURSE' as result_type, c.id as entity_id, ct.title as title,
                   left(coalesce(ct.description, ''), 240) as snippet,
                   ts_rank(ct.search_vector, q) as rank
            from courses c
            join course_translations ct on ct.course_id = c.id and ct.locale = :locale
            cross join websearch_to_tsquery(fts_config(:locale), :queryText) q
            where c.status = 'PUBLISHED' and ct.search_vector @@ q
            order by rank desc
            limit :limit offset :offset
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public SearchQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<SearchResult> search(String locale, String queryText, int limit, long offset) {
        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue("locale", locale)
                        .addValue("queryText", queryText)
                        .addValue("limit", limit)
                        .addValue("offset", offset);
        return jdbc.query(
                SQL,
                params,
                (rs, rowNum) ->
                        new SearchResult(
                                rs.getString("result_type"),
                                UUID.fromString(rs.getString("entity_id")),
                                rs.getString("title"),
                                rs.getString("snippet"),
                                rs.getDouble("rank")));
    }
}
