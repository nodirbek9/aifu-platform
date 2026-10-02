package uz.aifu.lms.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.time.LocalTime;
import java.util.List;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** OpenAPI 3.1 definition. The future frontend integrates from this; keep it accurate. */
@Configuration
public class OpenApiConfig {

    public static final String BEARER_SCHEME = "bearerAuth";

    static {
        // A time of day travels as the string "08:30:00", not as an {hour, minute, ...} object.
        SpringDocUtils.getConfig()
                .replaceWithSchema(
                        LocalTime.class, new StringSchema().format("time").example("08:30:00"));
    }

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

                                        **Async execution** — `POST /api/v1/submissions` (graded) and
                                        `POST /api/v1/runs` (not graded) only queue the work; poll
                                        `GET /api/v1/submissions/{id}` or `GET /api/v1/runs/{id}` until
                                        `status` is `COMPLETED` or `FAILED`.

                                        **Pagination** — list endpoints take `page` (from 0), `size` and
                                        `sort=property,asc|desc`, and answer a `PageResponse…` envelope.
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

    /**
     * Documents a Spring {@code Pageable} argument as what a client actually sends: the three query
     * parameters {@code page}, {@code size} and {@code sort}. Left alone, springdoc describes one
     * object-valued parameter called "pageable", and a generated client would send exactly that.
     */
    @Bean
    public GlobalOperationCustomizer pageableAsQueryParameters() {
        return (operation, handlerMethod) -> {
            if (operation.getParameters() == null
                    || !operation.getParameters().removeIf(p -> "pageable".equals(p.getName()))) {
                return operation;
            }
            operation.addParametersItem(
                    new QueryParameter()
                            .name("page")
                            .description("Zero-based page index")
                            .required(false)
                            .schema(new IntegerSchema()._default(0)));
            operation.addParametersItem(
                    new QueryParameter()
                            .name("size")
                            .description("Page size; capped by the server")
                            .required(false)
                            .schema(new IntegerSchema()._default(20)));
            operation.addParametersItem(
                    new QueryParameter()
                            .name("sort")
                            .description("property,asc or property,desc; may be repeated")
                            .required(false)
                            .schema(new ArraySchema().items(new StringSchema())));
            return operation;
        };
    }
}
