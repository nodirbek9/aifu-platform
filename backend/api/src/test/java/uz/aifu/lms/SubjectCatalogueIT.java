package uz.aifu.lms;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uz.aifu.lms.academics.api.dto.SubjectDtos.AddPrerequisiteRequest;
import uz.aifu.lms.academics.api.dto.SubjectDtos.ChangeSubjectStatusRequest;
import uz.aifu.lms.academics.api.dto.SubjectDtos.CreateSubjectRequest;
import uz.aifu.lms.academics.api.dto.SubjectDtos.SubjectDetail;
import uz.aifu.lms.academics.api.dto.SubjectDtos.UpsertSubjectTranslationRequest;
import uz.aifu.lms.academics.domain.RequirementType;
import uz.aifu.lms.academics.domain.SubjectKind;
import uz.aifu.lms.common.domain.PublicationStatus;
import uz.aifu.lms.common.error.ApiError;
import uz.aifu.lms.iam.api.dto.AuthDtos.LoginRequest;
import uz.aifu.lms.iam.api.dto.AuthDtos.TokenResponse;
import uz.aifu.lms.iam.domain.RoleCode;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.support.AbstractIntegrationTest;
import uz.aifu.lms.support.fixtures.AcademicFixtures;
import uz.aifu.lms.support.fixtures.UserFixtures;

/**
 * Phase 3 domain invariants (docs/domain/domain-model.md, CLAUDE.md §8): unique subject code, no
 * self-prerequisite, and "locale set on required translations is complete before publish" —
 * exercised end to end against real Postgres, not mocked.
 */
class SubjectCatalogueIT extends AbstractIntegrationTest {

    @Autowired private UserFixtures userFixtures;
    @Autowired private AcademicFixtures academicFixtures;
    @LocalServerPort private int port;

    @Test
    void create_duplicateCode_isRejectedAsConflict() {
        String adminToken = adminToken();
        String code = uniqueCode();
        assertThat(createSubject(adminToken, code).getStatusCode()).isEqualTo(HttpStatus.CREATED);

        HttpHeaders headers = bearer(adminToken);
        var department = academicFixtures.department();
        ResponseEntity<ApiError> conflict =
                rest.exchange(
                        url("/api/v1/admin/subjects"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new CreateSubjectRequest(
                                        code, department.getId(), SubjectKind.MANDATORY, "uz"),
                                headers),
                        ApiError.class);

        assertThat(conflict.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(conflict.getBody().code()).isEqualTo("RESOURCE_ALREADY_EXISTS");
    }

    @Test
    void addPrerequisite_selfReference_isRejected() {
        String adminToken = adminToken();
        SubjectDetail subject = createSubject(adminToken, uniqueCode()).getBody();

        HttpHeaders headers = bearer(adminToken);
        ResponseEntity<ApiError> response =
                rest.exchange(
                        url("/api/v1/admin/subjects/" + subject.id() + "/prerequisites"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new AddPrerequisiteRequest(subject.id(), null, null), headers),
                        ApiError.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("VALIDATION_FAILED");
    }

    @Test
    void addPrerequisite_differentSubject_succeeds() {
        String adminToken = adminToken();
        SubjectDetail subject = createSubject(adminToken, uniqueCode()).getBody();
        SubjectDetail other = createSubject(adminToken, uniqueCode()).getBody();

        HttpHeaders headers = bearer(adminToken);
        ResponseEntity<SubjectDetail> response =
                rest.exchange(
                        url("/api/v1/admin/subjects/" + subject.id() + "/prerequisites"),
                        HttpMethod.POST,
                        new HttpEntity<>(
                                new AddPrerequisiteRequest(
                                        other.id(), RequirementType.REQUIRED, null),
                                headers),
                        SubjectDetail.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().prerequisites()).hasSize(1);
        assertThat(response.getBody().prerequisites().get(0).prerequisiteSubjectId())
                .isEqualTo(other.id());
    }

    @Test
    void publish_withoutAnyTranslation_isRejectedAsIncomplete() {
        String adminToken = adminToken();
        SubjectDetail subject = createSubject(adminToken, uniqueCode()).getBody();

        ResponseEntity<ApiError> response =
                changeStatus(adminToken, subject.id(), PublicationStatus.PUBLISHED, ApiError.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().code()).isEqualTo("TRANSLATION_INCOMPLETE");
    }

    @Test
    void publish_afterAddingTheDefaultLocaleTranslation_succeeds() {
        String adminToken = adminToken();
        SubjectDetail subject = createSubject(adminToken, uniqueCode()).getBody();

        HttpHeaders headers = bearer(adminToken);
        ResponseEntity<SubjectDetail> upsert =
                rest.exchange(
                        url("/api/v1/admin/subjects/" + subject.id() + "/translations/uz"),
                        HttpMethod.PUT,
                        new HttpEntity<>(
                                new UpsertSubjectTranslationRequest(
                                        "Diskret matematika", null, null, null, null, null),
                                headers),
                        SubjectDetail.class);
        assertThat(upsert.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<SubjectDetail> published =
                changeStatus(
                        adminToken, subject.id(), PublicationStatus.PUBLISHED, SubjectDetail.class);

        assertThat(published.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(published.getBody().status()).isEqualTo(PublicationStatus.PUBLISHED);
        assertThat(published.getBody().publishedAt()).isNotNull();
    }

    private ResponseEntity<SubjectDetail> createSubject(String adminToken, String code) {
        HttpHeaders headers = bearer(adminToken);
        var department = academicFixtures.department();
        return rest.exchange(
                url("/api/v1/admin/subjects"),
                HttpMethod.POST,
                new HttpEntity<>(
                        new CreateSubjectRequest(
                                code, department.getId(), SubjectKind.MANDATORY, "uz"),
                        headers),
                SubjectDetail.class);
    }

    private <T> ResponseEntity<T> changeStatus(
            String adminToken, UUID subjectId, PublicationStatus status, Class<T> responseType) {
        HttpHeaders headers = bearer(adminToken);
        return rest.exchange(
                url("/api/v1/admin/subjects/" + subjectId + "/status"),
                HttpMethod.PUT,
                new HttpEntity<>(new ChangeSubjectStatusRequest(status), headers),
                responseType);
    }

    private String adminToken() {
        User admin = userFixtures.activeUser(RoleCode.ADMIN);
        ResponseEntity<TokenResponse> response =
                rest.postForEntity(
                        url("/api/v1/auth/login"),
                        new LoginRequest(admin.getUsername(), UserFixtures.RAW_PASSWORD),
                        TokenResponse.class);
        return response.getBody().accessToken();
    }

    private HttpHeaders bearer(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    private String uniqueCode() {
        return "SUB" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
