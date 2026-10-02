package uz.aifu.lms.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** OpenAPI 3.1 definition. The future frontend integrates from this; keep it accurate. */
@Configuration
public class OpenApiConfig {

    public static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI aifuOpenApi() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("AIFU Learning Platform API")
                                .version("v1")
                                .description(
                                        """
                                        Backend API for the AIFU Learning Platform.

                                        **Authentication** — `POST /api/v1/auth/login` returns a short-lived
                                        JWT access token (15 min) plus an opaque refresh token (30 days,
                                        single use, rotated). Send the access token as
                                        `Authorization: Bearer <token>`.

                                        **Errors** — every failure returns the `ApiError` envelope with a
                                        stable `code` your client may branch on, plus a `requestId` to quote
                                        in bug reports.

                                        **Localization** — content endpoints accept `?locale=uz|ru|en` and
                                        report `resolvedLocale` / `availableLocales` / `fallback` so the UI can
                                        flag untranslated content.

                                        **Async execution** — `POST /api/v1/submissions` returns `202` with a
                                        submission id; poll `GET /api/v1/submissions/{id}` until
                                        `status = COMPLETED`.
                                        """)
                                .contact(new Contact().name("AIFU Platform Team"))
                                .license(new License().name("Proprietary — AIFU")))
                .servers(
                        List.of(
                                new Server()
                                        .url("http://localhost:8080")
                                        .description("Local development"),
                                new Server()
                                        .url("https://lms.aifu.uz")
                                        .description("Production (placeholder)")))
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        BEARER_SCHEME,
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                                .description(
                                                        "Access token from /api/v1/auth/login")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
