# Build context is the repository root (see docker-compose.yml).
# The execution worker (ADR-006). Separate deployable from `api`. No inbound port, no web server.
# The ONLY component in this codebase that spawns a process to run untrusted student code
# (uz.aifu.lms.worker.sandbox.DockerRunSandboxEngine, legitimate only here per
# skills/coding-execution-security/SKILL.md — never in backend/api).

FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /build

# Module poms first (better layer caching on source-only changes).
COPY backend/pom.xml backend/pom.xml
COPY backend/api/pom.xml backend/api/pom.xml
COPY backend/execution-worker/pom.xml backend/execution-worker/pom.xml
COPY backend/mvnw backend/mvnw
COPY backend/.mvn backend/.mvn

WORKDIR /build/backend
RUN ./mvnw -q -pl execution-worker -B dependency:go-offline || true

WORKDIR /build
COPY backend/execution-worker/src backend/execution-worker/src

WORKDIR /build/backend
RUN ./mvnw -q -pl execution-worker -B package -DskipTests

FROM eclipse-temurin:21-jre-alpine AS runtime

# docker-cli: the docker-run sandbox engine invokes the HOST's docker daemon as a sibling
# container ("docker-outside-of-docker"), via the socket bind-mounted below. This means the
# worker container itself must run as root (it needs to read the mounted docker.sock) — that is a
# real, deliberate trade-off of the docker-run reference engine, not an oversight. It is exactly
# why ADR-006 names Judge0 (network-isolated, no socket access from the worker at all) the
# PRODUCTION default; docker-run exists for environments without Judge0 available. See
# docs/code-execution/security-model.md.
RUN apk add --no-cache docker-cli

WORKDIR /app
COPY --from=build /build/backend/execution-worker/target/execution-worker-*.jar /app/app.jar

ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
