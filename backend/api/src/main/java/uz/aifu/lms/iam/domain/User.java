package uz.aifu.lms.iam.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import uz.aifu.lms.common.domain.AuditableEntity;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.common.util.Guards;

/**
 * A platform account. Students, teachers and administrators are all users; what differs is the role
 * set and the optional profile ({@link StudentProfile} / {@link TeacherProfile}).
 *
 * <p>Invariants: the password hash is never plaintext and never logged; lockout state is owned here
 * (not in a cache) so it survives a restart.
 */
@Entity
@Table(name = "users")
public class User extends AuditableEntity {

    @Column(name = "email", nullable = false, unique = true, columnDefinition = "citext")
    private String email;

    @Column(name = "username", nullable = false, unique = true, columnDefinition = "citext")
    private String username;

    /** BCrypt hash. Never expose in any DTO, never log, never compare outside PasswordEncoder. */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status = UserStatus.PENDING;

    @Column(name = "preferred_locale", nullable = false)
    private String preferredLocale = LocaleCode.DEFAULT.code();

    @Column(name = "phone")
    private String phone;

    @Column(name = "must_change_password", nullable = false)
    private boolean mustChangePassword;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "password_changed_at")
    private Instant passwordChangedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new LinkedHashSet<>();

    protected User() {}

    public User(
            String email,
            String username,
            String passwordHash,
            String fullName,
            LocaleCode locale) {
        this.email = Guards.requiredText(email, "email").toLowerCase();
        this.username = Guards.requiredText(username, "username").toLowerCase();
        this.passwordHash = Guards.requiredText(passwordHash, "passwordHash");
        this.fullName = Guards.requiredText(fullName, "fullName");
        this.preferredLocale = (locale == null ? LocaleCode.DEFAULT : locale).code();
        this.status = UserStatus.PENDING;
    }

    // ---------------------------------------------------------------- behaviour

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public void changeStatus(UserStatus target) {
        this.status = Guards.required(target, "status");
    }

    public void changePassword(String newHash, Instant now) {
        this.passwordHash = Guards.requiredText(newHash, "passwordHash");
        this.passwordChangedAt = now;
        this.mustChangePassword = false;
        // Any successful password change clears a lockout: the legitimate owner has proven control.
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    /**
     * @return true when this failure caused a lockout.
     */
    public boolean registerFailedLogin(int maxAttempts, Duration lockoutDuration, Instant now) {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= maxAttempts) {
            this.lockedUntil = now.plus(lockoutDuration);
            return true;
        }
        return false;
    }

    public void registerSuccessfulLogin(Instant now) {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        this.lastLoginAt = now;
    }

    public boolean isLocked(Instant now) {
        return lockedUntil != null && lockedUntil.isAfter(now);
    }

    /**
     * Whether this account may obtain tokens right now. Deliberately does NOT explain why not: the
     * caller must return a single generic error (ADR-003) so accounts cannot be enumerated.
     */
    public boolean canAuthenticate(Instant now) {
        return status.canAuthenticate() && !isLocked(now);
    }

    public void updateProfile(String fullName, String phone, LocaleCode locale) {
        this.fullName = Guards.requiredText(fullName, "fullName");
        this.phone = phone;
        if (locale != null) {
            this.preferredLocale = locale.code();
        }
    }

    public void requirePasswordChange() {
        this.mustChangePassword = true;
    }

    // ---------------------------------------------------------------- accessors

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public UserStatus getStatus() {
        return status;
    }

    public LocaleCode getPreferredLocale() {
        return LocaleCode.find(preferredLocale).orElse(LocaleCode.DEFAULT);
    }

    public String getPhone() {
        return phone;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public Set<String> roleCodes() {
        return roles.stream().map(Role::getCode).collect(java.util.stream.Collectors.toSet());
    }

    public Set<String> permissionCodes() {
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getCode)
                .collect(java.util.stream.Collectors.toSet());
    }

    public void assignRole(Role role) {
        roles.add(Guards.required(role, "role"));
    }

    public void removeRole(Role role) {
        roles.remove(role);
    }
}
