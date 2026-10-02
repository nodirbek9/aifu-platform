package uz.aifu.lms.search.domain;

import java.util.UUID;

/**
 * One ranked hit. Not a JPA entity — the UNION-ranked query behind it spans two tables, so it is
 * hand-mapped from a native query row (see {@code search.persistence.SearchQueryRepository}).
 */
public record SearchResult(
        String resultType, UUID entityId, String title, String snippet, double rank) {}
