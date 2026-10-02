package uz.aifu.lms;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uz.aifu.lms.iam.api.dto.AuthDtos.ChangePasswordRequest;
import uz.aifu.lms.iam.api.dto.AuthDtos.CurrentUserResponse;
import uz.aifu.lms.iam.api.dto.AuthDtos.LoginRequest;
import uz.aifu.lms.iam.api.dto.AuthDtos.RefreshRequest;
import uz.aifu.lms.iam.api.dto.AuthDtos.TokenResponse;
import uz.aifu.lms.iam.domain.RoleCode;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.support.AbstractIntegrationTest;
import uz.aifu.lms.support.fixtures.UserFixtures;

/**
 * Phase 2 mandatory coverage (skills/testing/SKILL.md): login success/failure/lockout, token
 * refresh + rotation + reuse detection, expired/forged token rejection.
 */
class AuthenticationIT extends AbstractIntegrationTest {

    /** Must match {@code aifu.jwt.secret} in application-test.yml. */
    private static final String TEST_JWT_SECRET =
            "test_only_secret_value_used_exclusively_in_tests_0123456789";

    @Autowired private UserFixtures fixtures;
    @LocalServerPort private int port;

    @Test
    void login_correctCredentials_returnsTokenPair() {
        User user = fixtures.activeUser(RoleCode.STUDENT);

        ResponseEntity<TokenResponse> response =
                rest.postForEntity(
                        url("/api/v1/auth/login"),
                        new LoginRequest(user.getUsername(), UserFixtures.RAW_PASSWORD),
                        TokenResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        TokenResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.accessToken()).isNotBlank();
        assertThat(body.refreshToken()).isNotBlank();
        assertThat(body.tokenType()).isEqualTo("Bearer");
    }

    @Test
    void login_sameAccountAtTheSameInstant_everySignInSucceeds() throws Exception {
        User user = fixtures.activeUser(RoleCode.STUDENT);
        int signIns = 6;
        ExecutorService pool = Executors.newFixedThreadPool(signIns);
        CountDownLatch together = new CountDownLatch(1);
        try {
            List<Future<ResponseEntity<TokenResponse>>> answers = new ArrayList<>();
            for (int i = 0; i < signIns; i++) {
                answers.add(
                        pool.submit(
                                () -> {
                                    together.await();
                                    return rest.postForEntity(
                                            url("/api/v1/auth/login"),
                                            new LoginRequest(
                                                    user.getUsername(), UserFixtures.RAW_PASSWORD),
                                            TokenResponse.class);
                                }));
            }
            together.countDown();
            for (Future<ResponseEntity<TokenResponse>> answer : answers) {
                ResponseEntity<TokenResponse> response = answer.get(30, TimeUnit.SECONDS);
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().accessToken()).isNotBlank();
            }
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void login_wrongPassword_returnsGenericInvalidCredentials() {
        User user = fixtures.activeUser(RoleCode.STUDENT);

        ResponseEntity<String> response =
                rest.postForEntity(
                        url("/api/v1/auth/login"),
                        new LoginRequest(user.getUsername(), "not-the-password"),
                        String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).contains("\"code\":\"AUTH_INVALID_CREDENTIALS\"");
    }

    @Test
    void login_unknownIdentifier_returnsTheSameGenericError() {
        // No account enumeration (ADR-003): identical status/code whether the account exists or
        // not.
        ResponseEntity<String> response =
                rest.postForEntity(
                        url("/api/v1/auth/login"),
                        new LoginRequest("no-such-user-" + UUID.randomUUID(), "whatever-123!"),
                        String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).contains("\"code\":\"AUTH_INVALID_CREDENTIALS\"");
    }

    @Test
    void login_repeatedFailures_locksTheAccount() {
        User user = fixtures.activeUser(RoleCode.STUDENT);
        LoginRequest wrong = new LoginRequest(user.getUsername(), "still-wrong-1");

        // Default aifu.security.max-failed-login-attempts is 5; the 5th failure trips the lock.
        for (int i = 0; i < 5; i++) {
            rest.postForEntity(url("/api/v1/auth/login"), wrong, String.class);
        }

        // The next attempt uses the CORRECT password. If lockout works, it still fails.
        ResponseEntity<String> response =
                rest.postForEntity(
                        url("/api/v1/auth/login"),
                        new LoginRequest(user.getUsername(), UserFixtures.RAW_PASSWORD),
                        String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).contains("\"code\":\"AUTH_INVALID_CREDENTIALS\"");
    }

    @Test
    void refresh_rotatesTheToken_andTheOldTokenIsDead() {
        User user = fixtures.activeUser(RoleCode.STUDENT);
        TokenResponse original = login(user);

        ResponseEntity<TokenResponse> rotated =
                rest.postForEntity(
                        url("/api/v1/auth/refresh"),
                        new RefreshRequest(original.refreshToken()),
                        TokenResponse.class);
        assertThat(rotated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rotated.getBody().refreshToken()).isNotEqualTo(original.refreshToken());

        // Replaying the original (now-rotated) token must fail.
        ResponseEntity<String> replay =
                rest.postForEntity(
                        url("/api/v1/auth/refresh"),
                        new RefreshRequest(original.refreshToken()),
                        String.class);
        assertThat(replay.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void refresh_reusingAConsumedToken_revokesTheWholeFamily() {
        User user = fixtures.activeUser(RoleCode.STUDENT);
        TokenResponse original = login(user);

        ResponseEntity<TokenResponse> rotated =
                rest.postForEntity(
                        url("/api/v1/auth/refresh"),
                        new RefreshRequest(original.refreshToken()),
                        TokenResponse.class);
        assertThat(rotated.getStatusCode()).isEqualTo(HttpStatus.OK);
        String rotatedRefreshToken = rotated.getBody().refreshToken();

        // Replay the ORIGINAL again, simulating a leaked, already-consumed token.
        rest.postForEntity(
                url("/api/v1/auth/refresh"),
                new RefreshRequest(original.refreshToken()),
                String.class);

        // Reuse must revoke the whole family: even the legitimately-rotated successor is now dead.
        ResponseEntity<String> afterReuse =
                rest.postForEntity(
                        url("/api/v1/auth/refresh"),
                        new RefreshRequest(rotatedRefreshToken),
                        String.class);
        assertThat(afterReuse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void protectedEndpoint_withForgedToken_isRejected() {
        ResponseEntity<String> response = callMeWith("this.is-not.a-valid-jwt");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    /**
     * A request that security rejects is still traceable: the body names the same id as the {@code
     * X-Request-Id} header. (It used to say "null": the request-id filter ran after the security
     * chain.)
     */
    @Test
    void protectedEndpoint_withoutToken_isRejected_withItsRequestId() {
        ResponseEntity<String> response = rest.getForEntity(url("/api/v1/auth/me"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        String requestId = response.getHeaders().getFirst("X-Request-Id");
        assertThat(requestId).isNotBlank();
        assertThat(response.getBody())
                .contains("\"code\":\"AUTH_TOKEN_MISSING\"")
                .contains("\"requestId\":\"" + requestId + "\"");
    }

    /** An id with characters that are stripped must not break the request. */
    @Test
    void requestId_withUnsafeCharacters_isSanitized_notAnError() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Request-Id", "abc$%^&*()");
        ResponseEntity<String> response =
                rest.exchange(
                        url("/api/v1/auth/me"),
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getHeaders().getFirst("X-Request-Id")).isEqualTo("abc");
    }

    @Test
    void protectedEndpoint_withExpiredToken_isRejected() {
        User user = fixtures.activeUser(RoleCode.STUDENT);
        SecretKey key = Keys.hmacShaKeyFor(TEST_JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        String expiredToken =
                Jwts.builder()
                        .issuer("aifu-lms")
                        .subject(user.getId().toString())
                        .issuedAt(Date.from(Instant.now().minus(2, ChronoUnit.HOURS)))
                        .expiration(Date.from(Instant.now().minus(1, ChronoUnit.HOURS)))
                        .claim("username", user.getUsername())
                        .claim("roles", List.of("STUDENT"))
                        .claim("perms", List.of())
                        .signWith(key)
                        .compact();

        ResponseEntity<String> response = callMeWith(expiredToken);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void changePassword_revokesEveryExistingSession() {
        User user = fixtures.activeUser(RoleCode.STUDENT);
        TokenResponse tokens = login(user);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokens.accessToken());
        ResponseEntity<Void> changeResponse =
                rest.exchange(
                        url("/api/v1/auth/password"),
                        HttpMethod.PUT,
                        new HttpEntity<>(
                                new ChangePasswordRequest(
                                        UserFixtures.RAW_PASSWORD, "NewPass!456xyz"),
                                headers),
                        Void.class);
        assertThat(changeResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // The refresh token issued before the password change must now be dead.
        ResponseEntity<String> refreshAfter =
                rest.postForEntity(
                        url("/api/v1/auth/refresh"),
                        new RefreshRequest(tokens.refreshToken()),
                        String.class);
        assertThat(refreshAfter.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void me_returnsTheAuthenticatedCallersProfile() {
        User user = fixtures.activeUser(RoleCode.TEACHER);
        TokenResponse tokens = login(user);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokens.accessToken());
        ResponseEntity<CurrentUserResponse> response =
                rest.exchange(
                        url("/api/v1/auth/me"),
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        CurrentUserResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().id()).isEqualTo(user.getId());
        assertThat(response.getBody().roles()).contains("TEACHER");
    }

    private TokenResponse login(User user) {
        return rest.postForEntity(
                        url("/api/v1/auth/login"),
                        new LoginRequest(user.getUsername(), UserFixtures.RAW_PASSWORD),
                        TokenResponse.class)
                .getBody();
    }

    private ResponseEntity<String> callMeWith(String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        return rest.exchange(
                url("/api/v1/auth/me"), HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
