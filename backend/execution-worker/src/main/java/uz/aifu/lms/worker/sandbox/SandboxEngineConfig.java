package uz.aifu.lms.worker.sandbox;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uz.aifu.lms.worker.config.SandboxProperties;

/**
 * Selects the one {@link SandboxEngine} implementation named by {@code aifu.sandbox.engine}
 * (ADR-006). Fails fast at startup on an unknown value rather than silently falling back — an
 * unrecognised engine name must never resolve to "no sandboxing". {@link SandboxProperties} itself
 * is registered by {@code ExecutionWorkerApplication}'s {@code @ConfigurationPropertiesScan}.
 */
@Configuration
public class SandboxEngineConfig {

    @Bean
    public SandboxEngine sandboxEngine(SandboxProperties properties) {
        SandboxEngine engine =
                switch (properties.engine().toLowerCase()) {
                    case "judge0" -> new Judge0SandboxEngine(properties);
                    case "docker-run" -> new DockerRunSandboxEngine(properties);
                    case "noop" -> new NoopSandboxEngine();
                    default ->
                            throw new IllegalStateException(
                                    "Unknown aifu.sandbox.engine: "
                                            + properties.engine()
                                            + " (expected judge0 | docker-run | noop)");
                };
        if (!engine.supports(properties.engine())) {
            throw new IllegalStateException(
                    "Selected engine does not support its own configured key — bug");
        }
        return engine;
    }
}
