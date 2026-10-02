package uz.aifu.lms.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The execution worker (ADR-006). A separate deployable from {@code api} — no web server, no
 * inbound port, connects to PostgreSQL with the restricted role from V014. The only component in
 * this codebase that runs untrusted student code.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class ExecutionWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExecutionWorkerApplication.class, args);
    }
}
