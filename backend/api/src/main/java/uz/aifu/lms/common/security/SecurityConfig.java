package uz.aifu.lms.common.security;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import uz.aifu.lms.common.config.SecurityProperties;

/**
 * Stateless, deny-by-default HTTP security (ADR-003, ADR-004).
 *
 * <p>Route rules here are intentionally coarse. Fine-grained authorization is {@code @PreAuthorize}
 * on permissions plus the ownership guards in {@code iam.app.access} — never a URL pattern, because
 * URLs drift and guards do not.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_PATHS = {
        "/api/v1/auth/login",
        "/api/v1/auth/refresh",
        "/actuator/health",
        "/actuator/health/**",
        "/actuator/info",
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html"
    };

    private final SecurityProperties properties;

    public SecurityConfig(SecurityProperties properties) {
        this.properties = properties;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter)
            throws Exception {
        http.csrf(csrf -> csrf.disable()) // Stateless bearer tokens in a header; no
                // cookie auth. ADR-003.
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        registry ->
                                registry.requestMatchers(HttpMethod.OPTIONS, "/**")
                                        .permitAll()
                                        .requestMatchers(PUBLIC_PATHS)
                                        .permitAll()
                                        // Everything else requires authentication; permissions are
                                        // then
                                        // checked per handler with @PreAuthorize.
                                        .anyRequest()
                                        .authenticated())
                .exceptionHandling(
                        handling ->
                                handling.authenticationEntryPoint(
                                                new JsonAuthenticationEntryPoint())
                                        .accessDeniedHandler(new JsonAccessDeniedHandler()))
                .headers(
                        headers ->
                                headers.contentTypeOptions(Customizer.withDefaults())
                                        .frameOptions(frame -> frame.deny())
                                        .referrerPolicy(
                                                referrer ->
                                                        referrer.policy(
                                                                org.springframework.security.web
                                                                        .header.writers
                                                                        .ReferrerPolicyHeaderWriter
                                                                        .ReferrerPolicy
                                                                        .STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                                        .httpStrictTransportSecurity(
                                                hsts ->
                                                        hsts.includeSubDomains(true)
                                                                .maxAgeInSeconds(31_536_000))
                                        // The API returns JSON only; a restrictive CSP costs
                                        // nothing and
                                        // neutralises content sniffing of an error page.
                                        .contentSecurityPolicy(
                                                csp ->
                                                        csp.policyDirectives(
                                                                "default-src 'none'; frame-ancestors 'none'")))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** BCrypt cost 12. Swap to Argon2id here if AIFU security policy requires it (ADR-003). */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Explicit allowlist. Never "*" — this API is called with credentials.
        configuration.setAllowedOrigins(properties.corsAllowedOrigins());
        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "Accept-Language",
                        "X-Request-Id",
                        "Idempotency-Key"));
        configuration.setExposedHeaders(List.of("X-Request-Id", "Location", "Retry-After"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
