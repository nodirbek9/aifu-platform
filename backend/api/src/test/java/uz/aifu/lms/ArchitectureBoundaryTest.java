package uz.aifu.lms;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Enforces the module map in {@code docs/architecture/README.md} and the layering rules in {@code
 * skills/architecture/SKILL.md}. This is the ONE thing holding the modular-monolith architecture up
 * once more than one person (or one AI agent per session) touches this repo.
 *
 * <p>Registered here even for modules that have no Java yet, so the very first class added to
 * {@code academics} or any other not-yet-started module is already governed by the graph instead of
 * silently drifting until someone notices.
 */
class ArchitectureBoundaryTest {

    private static final String ROOT = "uz.aifu.lms";

    /**
     * Spring Data JPA's own auditing annotations ({@code @CreatedBy}/{@code @LastModifiedBy}/
     * {@code AuditingEntityListener}) are O/R-mapping metadata, the same category as {@code
     * jakarta.persistence.*} which domain classes already use freely — not business-logic coupling.
     * They are the one deliberate exception to "domain has no Spring imports"; everything else
     * under {@code org.springframework..} (stereotypes, web, security, transactions, ...) is still
     * forbidden.
     */
    private static final DescribedPredicate<JavaClass> SPRING_EXCEPT_JPA_AUDITING =
            resideInAPackage("org.springframework..")
                    .and(
                            not(
                                    resideInAnyPackage(
                                            "org.springframework.data.annotation..",
                                            "org.springframework.data.jpa.domain.support..")));

    /**
     * Every module named in CLAUDE.md §2 / docs/architecture/README.md. Add here when adding a
     * module.
     */
    private static final List<String> MODULES =
            List.of(
                    "common",
                    "audit",
                    "iam",
                    "academics",
                    "curriculum",
                    "enrollment",
                    "progress",
                    "assessment",
                    "coding",
                    "execution",
                    "grading",
                    "files",
                    "notification",
                    "analytics",
                    "search",
                    "scheduling",
                    "attendance");

    /**
     * Allowed-dependency graph, transcribed from the "Module map" diagram in {@code
     * docs/architecture/README.md}. {@code common} and {@code audit} are cross-cutting (everyone
     * may depend on them; {@code common} itself depends on nothing) and are therefore handled by
     * {@link #crossCuttingAllowanceFor(String)} rather than repeated in every entry.
     */
    private static final Map<String, Set<String>> ALLOWED =
            Map.ofEntries(
                    Map.entry("common", Set.of()),
                    Map.entry("audit", Set.of()),
                    Map.entry("iam", Set.of()),
                    Map.entry("academics", Set.of("iam")),
                    Map.entry("curriculum", Set.of("academics", "iam")),
                    Map.entry("enrollment", Set.of("academics", "iam")),
                    Map.entry("assessment", Set.of("academics", "curriculum", "enrollment", "iam")),
                    Map.entry("coding", Set.of("academics", "curriculum", "enrollment", "iam")),
                    Map.entry("execution", Set.of("coding", "enrollment", "iam")),
                    Map.entry(
                            "progress",
                            Set.of("enrollment", "curriculum", "assessment", "coding", "iam")),
                    Map.entry(
                            "grading",
                            Set.of(
                                    "enrollment",
                                    "assessment",
                                    "coding",
                                    "curriculum",
                                    "academics",
                                    "iam")),
                    Map.entry("analytics", Set.of("progress", "grading", "iam")),
                    Map.entry("search", Set.of("academics", "curriculum")),
                    Map.entry("notification", Set.of("iam")),
                    Map.entry("files", Set.of()),
                    Map.entry("scheduling", Set.of("academics", "iam")),
                    Map.entry(
                            "attendance",
                            Set.of("scheduling", "academics", "curriculum", "enrollment", "iam")));

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes =
                new ClassFileImporter()
                        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                        .importPackages(ROOT);
    }

    private static Set<String> crossCuttingAllowanceFor(String module) {
        if (module.equals("common")) {
            return Set.of();
        }
        if (module.equals("audit")) {
            return Set.of("common");
        }
        return Set.of("common", "audit");
    }

    @TestFactory
    List<DynamicTest> moduleDoesNotReachOutsideItsAllowedDependencies() {
        return MODULES.stream()
                .map(
                        module ->
                                DynamicTest.dynamicTest(
                                        module + " -> only {" + allowedSetFor(module) + "}",
                                        () -> {
                                            Set<String> forbidden = forbiddenTargetsFor(module);
                                            if (forbidden.isEmpty()) {
                                                return;
                                            }
                                            String[] forbiddenPackages =
                                                    forbidden.stream()
                                                            .map(other -> ROOT + "." + other + "..")
                                                            .toArray(String[]::new);
                                            ArchRule rule =
                                                    noClasses()
                                                            .that()
                                                            .resideInAPackage(
                                                                    ROOT + "." + module + "..")
                                                            .should()
                                                            .dependOnClassesThat()
                                                            .resideInAnyPackage(forbiddenPackages)
                                                            .because(
                                                                    "the module map in docs/architecture/README.md does not"
                                                                            + " allow "
                                                                            + module
                                                                            + " -> "
                                                                            + forbidden
                                                                            + " (if this is a genuine new need, write an ADR"
                                                                            + " and update the diagram + this test)")
                                                            .allowEmptyShould(true);
                                            rule.check(classes);
                                        }))
                .collect(Collectors.toList());
    }

    @TestFactory
    List<DynamicTest> noModuleReachesIntoAnotherModulesPersistencePackage() {
        return MODULES.stream()
                .map(
                        module ->
                                DynamicTest.dynamicTest(
                                        module
                                                + " never imports another module's persistence package",
                                        () -> {
                                            List<String> others =
                                                    MODULES.stream()
                                                            .filter(m -> !m.equals(module))
                                                            .toList();
                                            if (others.isEmpty()) {
                                                return;
                                            }
                                            String[] otherPersistencePackages =
                                                    others.stream()
                                                            .map(
                                                                    other ->
                                                                            ROOT
                                                                                    + "."
                                                                                    + other
                                                                                    + ".persistence..")
                                                            .toArray(String[]::new);
                                            ArchRule rule =
                                                    noClasses()
                                                            .that()
                                                            .resideInAPackage(
                                                                    ROOT + "." + module + "..")
                                                            .and()
                                                            .resideOutsideOfPackage(
                                                                    ROOT
                                                                            + "."
                                                                            + module
                                                                            + ".persistence..")
                                                            .should()
                                                            .dependOnClassesThat()
                                                            .resideInAnyPackage(
                                                                    otherPersistencePackages)
                                                            .because(
                                                                    "cross-module writes go through the owning module's app"
                                                                            + " service, never its repositories directly"
                                                                            + " (CLAUDE.md §2)")
                                                            .allowEmptyShould(true);
                                            rule.check(classes);
                                        }))
                .collect(Collectors.toList());
    }

    @TestFactory
    List<DynamicTest> domainLayerIsInnermost() {
        return MODULES.stream()
                .map(
                        module ->
                                DynamicTest.dynamicTest(
                                        module
                                                + ".domain depends on nothing but itself, the JDK and jakarta.*",
                                        () -> {
                                            String base = ROOT + "." + module;
                                            noClasses()
                                                    .that()
                                                    .resideInAPackage(base + ".domain..")
                                                    .should()
                                                    .dependOnClassesThat()
                                                    .resideInAnyPackage(
                                                            base + ".api..",
                                                            base + ".app..",
                                                            base + ".persistence..")
                                                    .because(
                                                            "domain must not depend on its own module's api/app/persistence"
                                                                    + " layers (skills/architecture/SKILL.md rule 1)")
                                                    .allowEmptyShould(true)
                                                    .check(classes);
                                            noClasses()
                                                    .that()
                                                    .resideInAPackage(base + ".domain..")
                                                    .should()
                                                    .dependOnClassesThat(SPRING_EXCEPT_JPA_AUDITING)
                                                    .because(
                                                            "domain entities/value objects must have no Spring imports"
                                                                    + " beyond Spring Data JPA's own auditing annotations"
                                                                    + " (CLAUDE.md §6)")
                                                    .allowEmptyShould(true)
                                                    .check(classes);
                                        }))
                .collect(Collectors.toList());
    }

    @TestFactory
    List<DynamicTest> persistenceLayerNeverCallsUpwardIntoAppOrApi() {
        return MODULES.stream()
                .map(
                        module ->
                                DynamicTest.dynamicTest(
                                        module
                                                + ".persistence does not depend on "
                                                + module
                                                + ".app/api",
                                        () -> {
                                            String base = ROOT + "." + module;
                                            noClasses()
                                                    .that()
                                                    .resideInAPackage(base + ".persistence..")
                                                    .should()
                                                    .dependOnClassesThat()
                                                    .resideInAnyPackage(
                                                            base + ".api..", base + ".app..")
                                                    .because(
                                                            "persistence -> domain only (CLAUDE.md §2)")
                                                    .allowEmptyShould(true)
                                                    .check(classes);
                                        }))
                .collect(Collectors.toList());
    }

    @TestFactory
    List<DynamicTest> controllersNeverInjectARepository() {
        return MODULES.stream()
                .map(
                        module ->
                                DynamicTest.dynamicTest(
                                        module
                                                + ".api does not depend on "
                                                + module
                                                + ".persistence",
                                        () -> {
                                            String base = ROOT + "." + module;
                                            noClasses()
                                                    .that()
                                                    .resideInAPackage(base + ".api..")
                                                    .should()
                                                    .dependOnClassesThat()
                                                    .resideInAPackage(base + ".persistence..")
                                                    .because(
                                                            "controllers never inject a repository"
                                                                    + " (skills/architecture/SKILL.md rule 6)")
                                                    .allowEmptyShould(true)
                                                    .check(classes);
                                        }))
                .collect(Collectors.toList());
    }

    private static Set<String> forbiddenTargetsFor(String module) {
        Set<String> allowed = ALLOWED.getOrDefault(module, Set.of());
        Set<String> crossCutting = crossCuttingAllowanceFor(module);
        return MODULES.stream()
                .filter(other -> !other.equals(module))
                .filter(other -> !allowed.contains(other))
                .filter(other -> !crossCutting.contains(other))
                .collect(Collectors.toCollection(java.util.TreeSet::new));
    }

    private static String allowedSetFor(String module) {
        Set<String> all = new java.util.TreeSet<>(ALLOWED.getOrDefault(module, Set.of()));
        all.addAll(crossCuttingAllowanceFor(module));
        return String.join(", ", all);
    }
}
