package uz.aifu.lms;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uz.aifu.lms.academics.domain.Course;
import uz.aifu.lms.enrollment.api.dto.EnrollmentDtos.CreateEnrollmentRequest;
import uz.aifu.lms.enrollment.api.dto.EnrollmentDtos.EnrollmentDetail;
import uz.aifu.lms.iam.api.dto.AuthDtos.LoginRequest;
import uz.aifu.lms.iam.api.dto.AuthDtos.TokenResponse;
import uz.aifu.lms.iam.domain.RoleCode;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.support.AbstractIntegrationTest;
import uz.aifu.lms.support.fixtures.EnrollmentFixtures;
import uz.aifu.lms.support.fixtures.UserFixtures;

/**
 * Phase 4 DoD (docs/planning/phase-plan.md): "EnrollmentConcurrencyIT proves the DB index holds."
 * The app-level pre-check in {@code EnrollmentService.enroll} is a fast-path convenience, not the
 * real guarantee — two requests racing past it simultaneously is exactly what {@code
 * uq_enrollment_active} (V006) exists for, and what {@code saveAndFlush} + the catch around it
 * (rather than the deferred-flush {@code save}) is written to surface correctly.
 */
class EnrollmentConcurrencyIT extends AbstractIntegrationTest {

    @Autowired private UserFixtures userFixtures;
    @Autowired private EnrollmentFixtures enrollmentFixtures;
    @LocalServerPort private int port;

    @Test
    void twoConcurrentEnrollRequests_onlyOneSucceeds() throws Exception {
        String adminToken = login(userFixtures.activeUser(RoleCode.ADMIN));
        Course course = enrollmentFixtures.publishedCourse(150);
        User student = userFixtures.activeUser(RoleCode.STUDENT);

        int attempts = 8;
        CountDownLatch ready = new CountDownLatch(attempts);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(attempts);
        AtomicInteger created = new AtomicInteger();
        AtomicInteger conflicted = new AtomicInteger();
        List<Integer> statuses =
                java.util.Collections.synchronizedList(new java.util.ArrayList<>());

        try {
            List<java.util.concurrent.Future<?>> futures = new java.util.ArrayList<>();
            for (int i = 0; i < attempts; i++) {
                futures.add(
                        pool.submit(
                                () -> {
                                    ready.countDown();
                                    try {
                                        start.await();
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                        return;
                                    }
                                    ResponseEntity<EnrollmentDetail> response =
                                            rest.exchange(
                                                    url("/api/v1/admin/enrollments"),
                                                    HttpMethod.POST,
                                                    new HttpEntity<>(
                                                            new CreateEnrollmentRequest(
                                                                    course.getId(),
                                                                    student.getId(),
                                                                    null),
                                                            bearer(adminToken)),
                                                    EnrollmentDetail.class);
                                    statuses.add(response.getStatusCode().value());
                                    if (response.getStatusCode() == HttpStatus.CREATED) {
                                        created.incrementAndGet();
                                    } else if (response.getStatusCode() == HttpStatus.CONFLICT) {
                                        conflicted.incrementAndGet();
                                    }
                                }));
            }
            ready.await(10, TimeUnit.SECONDS);
            start.countDown();
            for (var future : futures) {
                future.get(30, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdown();
        }

        assertThat(created.get())
                .as("exactly one of %s concurrent requests must win: %s", attempts, statuses)
                .isEqualTo(1);
        assertThat(conflicted.get()).isEqualTo(attempts - 1);

        List<uz.aifu.lms.enrollment.api.dto.EnrollmentDtos.EnrollmentSummary> roster =
                listByCourse(adminToken, course.getId());
        assertThat(roster).hasSize(1);
        assertThat(roster.get(0).status())
                .isEqualTo(uz.aifu.lms.enrollment.domain.EnrollmentStatus.ACTIVE);
    }

    private List<uz.aifu.lms.enrollment.api.dto.EnrollmentDtos.EnrollmentSummary> listByCourse(
            String adminToken, java.util.UUID courseId) {
        ResponseEntity<
                        uz.aifu.lms.common.api.PageResponse<
                                uz.aifu.lms.enrollment.api.dto.EnrollmentDtos.EnrollmentSummary>>
                response =
                        rest.exchange(
                                url("/api/v1/admin/enrollments?courseId=" + courseId),
                                HttpMethod.GET,
                                new HttpEntity<>(bearer(adminToken)),
                                new org.springframework.core.ParameterizedTypeReference<>() {});
        return response.getBody().content();
    }

    private String login(User user) {
        ResponseEntity<TokenResponse> response =
                rest.postForEntity(
                        url("/api/v1/auth/login"),
                        new LoginRequest(user.getUsername(), UserFixtures.RAW_PASSWORD),
                        TokenResponse.class);
        return response.getBody().accessToken();
    }

    private HttpHeaders bearer(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
