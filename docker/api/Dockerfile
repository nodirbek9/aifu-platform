# Build context is the repository root (see docker-compose.yml).
# Modular monolith HTTP API. Owns the Flyway migration history. NEVER executes untrusted code
# (enforced at build time by uz.aifu.lms.NoProcessExecutionInApiTest) — see ADR-001, ADR-006.

FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /build

# Module poms first (better layer caching on source-only changes) — the reactor needs every
# declared module's pom present even though only `api` is actually built below.
COPY backend/pom.xml backend/pom.xml
COPY backend/api/pom.xml backend/api/pom.xml
COPY backend/execution-worker/pom.xml backend/execution-worker/pom.xml
COPY backend/mvnw backend/mvnw
COPY backend/.mvn backend/.mvn

WORKDIR /build/backend
RUN ./mvnw -q -pl api -B dependency:go-offline || true

WORKDIR /build
COPY backend/api/src backend/api/src

WORKDIR /build/backend
RUN ./mvnw -q -pl api -B package -DskipTests

FROM eclipse-temurin:21-jre-alpine AS runtime

# Non-root: the API process has no legitimate reason to run as uid 0 (CLAUDE.md §7).
RUN addgroup -S aifu && adduser -S aifu -G aifu
# The local storage root (STORAGE_LOCAL_ROOT, a named volume in docker-compose.yml) must be
# writable by this user. Docker seeds a new named volume with this directory and its owner;
# without it the volume is root-owned and every upload fails with STORAGE_UNAVAILABLE.
RUN mkdir -p /var/lib/aifu/storage && chown aifu:aifu /var/lib/aifu/storage
USER aifu

WORKDIR /app
COPY --from=build --chown=aifu:aifu /build/backend/api/target/api-*.jar /app/app.jar

EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

HEALTHCHECK --interval=15s --timeout=5s --start-period=40s --retries=5 \
    CMD wget -qO- http://localhost:8080/actuator/health/liveness || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
