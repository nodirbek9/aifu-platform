package uz.aifu.lms.search.domain;

import java.util.UUID;

/**
 * One ranked hit. Not a JPA entity — the UNION-ranked query behind it spans several tables, so it
 * is hand-mapped from a native query row (see {@code search.persistence.SearchQueryRepository}).
 *
 * @param courseId the course a hit is opened in: the course itself, the course of a lesson; {@code
 *     null} for a subject
 */
public record SearchResult(
        String resultType,
        UUID entityId,
        UUID courseId,
        String title,
        String snippet,
        double rank) {}
