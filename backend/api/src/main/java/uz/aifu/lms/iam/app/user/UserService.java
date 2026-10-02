package uz.aifu.lms.iam.app.user;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.audit.app.AuditService;
import uz.aifu.lms.audit.domain.AuditAction;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.iam.api.dto.UserDtos.CreateUserRequest;
import uz.aifu.lms.iam.api.dto.UserDtos.UpdateUserRequest;
import uz.aifu.lms.iam.app.auth.RefreshTokenService;
import uz.aifu.lms.iam.domain.Department;
import uz.aifu.lms.iam.domain.Permission;
import uz.aifu.lms.iam.domain.RefreshToken;
import uz.aifu.lms.iam.domain.Role;
import uz.aifu.lms.iam.domain.RoleCode;
import uz.aifu.lms.iam.domain.StudentProfile;
import uz.aifu.lms.iam.domain.TeacherProfile;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.iam.domain.UserStatus;
import uz.aifu.lms.iam.persistence.DepartmentRepository;
import uz.aifu.lms.iam.persistence.PermissionRepository;
import uz.aifu.lms.iam.persistence.RoleRepository;
import uz.aifu.lms.iam.persistence.StudentProfileRepository;
import uz.aifu.lms.iam.persistence.TeacherProfileRepository;
import uz.aifu.lms.iam.persistence.UserRepository;

/**
 * Account administration (brief §22).
 *
 * <p>Self-service registration does not exist: AIFU accounts are provisioned by an administrator
 * from institutional records. That is a product decision, not an oversight, and it removes a whole
 * class of abuse (fake accounts, email-verification flows, enumeration via signup).
 *
 * <p>Role assignment is the most dangerous operation here, so it is separated from profile edits,
 * requires its own permission, and is always audited with the before/after role sets.
 */
@Service
public class UserService {

    private final UserRepository users;
    private final RoleRepository roles;
    private final PermissionRepository permissions;
    private final StudentProfileRepository studentProfiles;
    private final TeacherProfileRepository teacherProfiles;
    private final DepartmentRepository departments;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokens;
    private final AuditService audit;
    private final Clock clock;

    public UserService(
            UserRepository users,
            RoleRepository roles,
            PermissionRepository permissions,
            StudentProfileRepository studentProfiles,
            TeacherProfileRepository teacherProfiles,
            DepartmentRepository departments,
            PasswordEncoder passwordEncoder,
            RefreshTokenService refreshTokens,
            AuditService audit,
            Clock clock) {
        this.users = users;
        this.roles = roles;
        this.permissions = permissions;
        this.studentProfiles = studentProfiles;
        this.teacherProfiles = teacherProfiles;
        this.departments = departments;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokens = refreshTokens;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Page<User> search(String query, UserStatus status, Pageable pageable) {
        // Pre-formatted here, not via JPQL concat(): a null :pattern bound through concat() left
        // Hibernate to guess the parameter's JDBC type, and it guessed bytea against Postgres
        // ("function lower(bytea) does not exist") — passing the finished LIKE pattern (or null)
        // gives the parameter one unambiguous use, which resolves correctly.
        String normalized = (query == null || query.isBlank()) ? null : query.trim();
        String pattern = normalized == null ? null : "%" + normalized.toLowerCase() + "%";
        return users.search(pattern, status, pageable);
    }

    /**
     * Batched role codes for exactly the given user ids — pair with {@link #search} to render a
     * result page without an N+1 and without the collection-fetch-pagination error.
     */
    @Transactional(readOnly = true)
    public Map<UUID, Set<String>> rolesByUserIds(Collection<UUID> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, Set<String>> result = new LinkedHashMap<>();
        for (UserRepository.UserRoleCodeProjection row : users.findRoleCodesForUsers(userIds)) {
            result.computeIfAbsent(row.getUserId(), ignored -> new LinkedHashSet<>())
                    .add(row.getRoleCode());
        }
        return result;
    }

    @Transactional(readOnly = true)
    public User require(UUID id) {
        return users.findWithAuthoritiesById(id).orElseThrow(() -> Exceptions.notFound("User", id));
    }

    /**
     * Records one failed login attempt (and locks the account on the final one).
     *
     * <p>{@code REQUIRES_NEW} is load-bearing: {@code AuthenticationService.login} calls this and
     * then throws {@code AUTH_INVALID_CREDENTIALS} to reject the request. Spring rolls back the
     * whole physical transaction on that unchecked exception, which would silently undo the counter
     * increment / lockout this call just made — the account would never actually lock no matter how
     * many bad passwords were submitted. Committing independently, before the caller's exception
     * can touch it, is the only way the bookkeeping survives.
     *
     * @return true when this failure just triggered a lockout
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean recordFailedLogin(
            UUID userId, int maxAttempts, Duration lockoutDuration, Instant now) {
        User user = require(userId);
        return user.registerFailedLogin(maxAttempts, lockoutDuration, now);
    }

    @Transactional(readOnly = true)
    public Optional<StudentProfile> studentProfile(UUID userId) {
        return studentProfiles.findById(userId);
    }

    @Transactional(readOnly = true)
    public Optional<TeacherProfile> teacherProfile(UUID userId) {
        return teacherProfiles.findWithDepartmentByUserId(userId);
    }

    /**
     * Backs {@code enrollment}'s CSV roster import — resolves a registrar-supplied student number.
     */
    @Transactional(readOnly = true)
    public Optional<UUID> findUserIdByStudentNumber(String studentNumber) {
        return studentProfiles.findByStudentNumber(studentNumber).map(StudentProfile::getUserId);
    }

    /** Backs the admin role-management screen (Phase 10, ADR-004): roles are data, listed as-is. */
    @Transactional(readOnly = true)
    public List<Role> listRoles() {
        return roles.findAllWithPermissions();
    }

    /** The full permission catalogue, for the same admin screen. Seeded read-only data (V003). */
    @Transactional(readOnly = true)
    public List<Permission> listPermissions() {
        return permissions.findAllOrdered();
    }

    /**
     * Creates an account.
     *
     * <p>Uniqueness is checked here for a clean error message and enforced by the database's unique
     * indexes for correctness under concurrency. The pre-check is a courtesy; the constraint is the
     * guarantee.
     */
    @Transactional
    public User create(CreateUserRequest request, UUID actorId, String ip) {
        if (users.existsByEmailIgnoreCase(request.email())) {
            throw Exceptions.conflict(
                    ErrorCode.RESOURCE_ALREADY_EXISTS, "Email is already registered");
        }
        if (users.existsByUsernameIgnoreCase(request.username())) {
            throw Exceptions.conflict(
                    ErrorCode.RESOURCE_ALREADY_EXISTS, "Username is already taken");
        }

        Set<Role> resolvedRoles = resolveRoles(request.roles());
        boolean isStudent = containsRole(resolvedRoles, RoleCode.STUDENT);
        boolean isTeacher = containsRole(resolvedRoles, RoleCode.TEACHER);

        if (isStudent && request.studentProfile() == null) {
            throw Exceptions.validation(
                    "studentProfile is required when the STUDENT role is assigned");
        }

        // A password the administrator never sets cannot be guessed: the account stays unusable
        // until
        // an explicit reset. The placeholder hash is a random value, not a known constant.
        String hash =
                request.initialPassword() == null
                        ? passwordEncoder.encode(UUID.randomUUID() + ":" + UUID.randomUUID())
                        : passwordEncoder.encode(request.initialPassword());

        LocaleCode locale = LocaleCode.parseOrDefault(request.preferredLocale());
        User user =
                new User(
                        request.email().trim(),
                        request.username().trim(),
                        hash,
                        request.fullName().trim(),
                        locale);
        user.updateProfile(request.fullName().trim(), request.phone(), locale);
        resolvedRoles.forEach(user::assignRole);

        if (request.initialPassword() == null) {
            user.changeStatus(UserStatus.PENDING);
            user.requirePasswordChange();
        } else {
            user.activate();
            // Default to true: an administrator-chosen password is known to someone other than the
            // user.
            if (request.mustChangePassword() == null || request.mustChangePassword()) {
                user.requirePasswordChange();
            }
        }

        User saved = users.save(user);

        if (isStudent) {
            var profile = request.studentProfile();
            studentProfiles.save(
                    new StudentProfile(
                            saved,
                            profile.studentNumber().trim(),
                            profile.programmeId(),
                            profile.groupCode(),
                            profile.enrollmentYear()));
        }
        if (isTeacher && request.teacherProfile() != null) {
            var profile = request.teacherProfile();
            Department department =
                    profile.departmentId() == null
                            ? null
                            : departments
                                    .findById(profile.departmentId())
                                    .orElseThrow(
                                            () ->
                                                    Exceptions.notFound(
                                                            "Department", profile.departmentId()));
            teacherProfiles.save(
                    new TeacherProfile(
                            saved, profile.employeeNumber(), department, profile.academicTitle()));
        }

        audit.record(
                AuditAction.USER_CREATED,
                actorId,
                "user",
                saved.getId(),
                ip,
                null,
                Map.of("username", saved.getUsername(), "roles", saved.roleCodes()));
        return saved;
    }

    @Transactional
    public User updateProfile(UUID userId, UpdateUserRequest request, UUID actorId, String ip) {
        User user = require(userId);
        user.updateProfile(
                request.fullName() == null ? user.getFullName() : request.fullName().trim(),
                request.phone() == null ? user.getPhone() : request.phone(),
                request.preferredLocale() == null
                        ? user.getPreferredLocale()
                        : LocaleCode.parseOrDefault(request.preferredLocale()));
        audit.record(AuditAction.USER_UPDATED, actorId, "user", userId, ip, null);
        return user;
    }

    /**
     * Changes the account lifecycle status.
     *
     * <p>Any move away from ACTIVE revokes every refresh token: a suspended account must lose
     * access immediately, not when its 15-minute access token happens to expire. Access tokens
     * themselves are stateless, so {@code JwtAuthenticationFilter} re-checks status on each request
     * for the same reason.
     */
    @Transactional
    public User changeStatus(
            UUID userId, UserStatus target, String reason, UUID actorId, String ip) {
        User user = require(userId);
        if (userId.equals(actorId) && target != UserStatus.ACTIVE) {
            // Locking yourself out of the only admin account is an outage, not an operation.
            throw Exceptions.rule(
                    ErrorCode.INVALID_STATE_TRANSITION, "You cannot deactivate your own account");
        }
        UserStatus previous = user.getStatus();
        user.changeStatus(target);
        if (target != UserStatus.ACTIVE) {
            refreshTokens.revokeAllForUser(userId, RefreshToken.RevocationReason.ACCOUNT_DISABLED);
        }
        audit.record(
                AuditAction.USER_STATUS_CHANGED,
                actorId,
                "user",
                userId,
                ip,
                null,
                reason == null
                        ? Map.of("from", previous, "to", target)
                        : Map.of("from", previous, "to", target, "reason", reason));
        return user;
    }

    /** Replaces the role set wholesale. Audited with both the old and the new set. */
    @Transactional
    public User setRoles(UUID userId, Set<String> roleCodes, UUID actorId, String ip) {
        User user = require(userId);
        Set<String> before = new LinkedHashSet<>(user.roleCodes());
        Set<Role> target = resolveRoles(roleCodes);

        if (userId.equals(actorId)
                && target.stream().noneMatch(r -> RoleCode.ADMIN.equals(r.getCode()))) {
            throw Exceptions.rule(
                    ErrorCode.INVALID_STATE_TRANSITION, "You cannot remove your own ADMIN role");
        }

        new HashSet<>(user.getRoles()).forEach(user::removeRole);
        target.forEach(user::assignRole);

        // A changed role set changes the token claims, so old tokens must not survive.
        refreshTokens.revokeAllForUser(userId, RefreshToken.RevocationReason.ROLES_CHANGED);

        audit.record(
                AuditAction.ROLE_GRANTED,
                actorId,
                "user",
                userId,
                ip,
                null,
                Map.of("before", before, "after", user.roleCodes()));
        return user;
    }

    /** Administrative reset. The user must choose their own password at next login. */
    @Transactional
    public void resetPassword(UUID userId, String newPassword, UUID actorId, String ip) {
        User user = require(userId);
        user.changePassword(passwordEncoder.encode(newPassword), clock.instant());
        user.requirePasswordChange();
        refreshTokens.revokeAllForUser(userId, RefreshToken.RevocationReason.PASSWORD_CHANGED);
        audit.record(AuditAction.USER_PASSWORD_RESET, actorId, "user", userId, ip, null);
    }

    /** Clears a brute-force lockout without touching the password. */
    @Transactional
    public void unlock(UUID userId, UUID actorId, String ip) {
        User user = require(userId);
        user.registerSuccessfulLogin(clock.instant());
        audit.record(
                AuditAction.USER_UPDATED,
                actorId,
                "user",
                userId,
                ip,
                null,
                Map.of("operation", "unlock"));
    }

    private Set<Role> resolveRoles(Set<String> codes) {
        if (codes == null || codes.isEmpty()) {
            throw Exceptions.validation("At least one role is required");
        }
        Set<Role> resolved = new LinkedHashSet<>();
        for (String code : codes) {
            String normalized = code == null ? "" : code.trim().toUpperCase();
            resolved.add(
                    roles.findByCode(normalized)
                            .orElseThrow(
                                    () -> Exceptions.validation("Unknown role: " + normalized)));
        }
        return resolved;
    }

    private static boolean containsRole(Set<Role> resolved, String code) {
        return resolved.stream().anyMatch(r -> code.equals(r.getCode()));
    }
}
