package uz.aifu.lms;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

/**
 * CLAUDE.md §7.1, non-negotiable: student code is never executed in the API process. The only
 * legitimate path is persist {@code submission} -> enqueue {@code execution_job} -> worker ->
 * sandbox (ADR-006). This test fails the build the moment that boundary is crossed.
 *
 * <p>Only {@code backend/api} is scanned (this test lives in the {@code api} module and its
 * ArchUnit import is scoped to {@code uz.aifu.lms}). {@code backend/execution-worker} is a separate
 * deployable that is explicitly allowed to talk to a sandbox adapter.
 */
class NoProcessExecutionInApiTest {

    private static final JavaClasses CLASSES =
            new ClassFileImporter()
                    .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                    .importPackages("uz.aifu.lms");

    @Test
    void apiNeverConstructsAnOsProcess() {
        ArchRule rule =
                noClasses()
                        .should()
                        .dependOnClassesThat()
                        .belongToAnyOf(ProcessBuilder.class, Runtime.class, Process.class)
                        .because(
                                "untrusted code execution must go through submission -> execution_job -> worker"
                                        + " -> sandbox, never Runtime/ProcessBuilder in the API process (ADR-006)");
        rule.check(CLASSES);
    }

    @Test
    void apiNeverUsesAScriptingEngine() {
        ArchRule rule =
                noClasses()
                        .should()
                        .dependOnClassesThat()
                        .resideInAPackage("javax.script..")
                        .because(
                                "a JSR-223 scripting engine is an in-process code-execution path, forbidden in"
                                        + " backend/api (ADR-006)");
        rule.check(CLASSES);
    }

    // Deliberately not enforced here: reflective Method/Constructor invocation used as a
    // dynamic-eval mechanism cannot be distinguished structurally from Spring's own pervasive use
    // of reflection. The two bans above cover the actual exploitable primitives (spawning a host
    // process, or handing source text to an interpreter); anything more exotic is a code-review
    // concern, not a static-analysis one.
}
