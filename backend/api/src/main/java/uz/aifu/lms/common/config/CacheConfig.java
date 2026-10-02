package uz.aifu.lms.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.List;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * In-process caching (Caffeine, no Redis — ADR-002).
 *
 * <p>Only locale-independent, authorization-independent reference data is cached. NEVER cache
 * student-scoped data: a cache key that omits the caller is a data-leak bug waiting to happen.
 */
@Configuration
public class CacheConfig {

    public static final String SUBJECTS = "subjects";
    public static final String COURSES = "courses";
    public static final String COURSE_STRUCTURE = "course-structure";
    public static final String ROLE_PERMISSIONS = "role-permissions";
    public static final String PROGRAMMING_LANGUAGES = "programming-languages";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager =
                new CaffeineCacheManager(
                        SUBJECTS,
                        COURSES,
                        COURSE_STRUCTURE,
                        ROLE_PERMISSIONS,
                        PROGRAMMING_LANGUAGES);
        manager.setCaffeine(
                Caffeine.newBuilder()
                        .maximumSize(2_000)
                        .expireAfterWrite(Duration.ofMinutes(10))
                        .recordStats());
        manager.setCacheNames(
                List.of(
                        SUBJECTS,
                        COURSES,
                        COURSE_STRUCTURE,
                        ROLE_PERMISSIONS,
                        PROGRAMMING_LANGUAGES));
        return manager;
    }
}
