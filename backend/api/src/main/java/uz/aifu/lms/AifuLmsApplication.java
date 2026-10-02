package uz.aifu.lms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * AIFU Learning Platform — HTTP API.
 *
 * <p>Modular monolith (ADR-001). This process NEVER executes untrusted student code: submissions
 * are persisted and enqueued, and the separate {@code execution-worker} deployable is the only
 * component that touches a sandbox. See {@code docs/decisions/ADR-006-code-execution.md}.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableCaching
@EnableScheduling
@EnableJpaAuditing
@EnableTransactionManagement
public class AifuLmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AifuLmsApplication.class, args);
    }
}
