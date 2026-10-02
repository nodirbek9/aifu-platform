package uz.aifu.lms.common.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Inject this everywhere instead of calling {@code Instant.now()}. Tests substitute {@code
     * Clock.fixed(...)}, which is the only way time-dependent logic (attempt expiry, token TTL,
     * lockout windows) can be tested deterministically.
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
