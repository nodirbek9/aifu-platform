package uz.aifu.lms.support.fixtures;

import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.iam.domain.Role;
import uz.aifu.lms.iam.domain.RoleCode;
import uz.aifu.lms.iam.domain.StudentProfile;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.iam.persistence.RoleRepository;
import uz.aifu.lms.iam.persistence.StudentProfileRepository;
import uz.aifu.lms.iam.persistence.UserRepository;

/**
 * Builds persisted, ACTIVE users directly via the repositories — not through {@code UserService} or
 * the HTTP API — so authentication/authorization tests can set up a caller in one line without
 * depending on the admin-provisioning flow they are not testing (skills/testing/SKILL.md).
 */
@Component
public class UserFixtures {

    /** Known in every fixture user; tests authenticate with this. */
    public static final String RAW_PASSWORD = "Test!Pass123";

    private final UserRepository users;
    private final RoleRepository roles;
    private final StudentProfileRepository studentProfiles;
    private final PasswordEncoder passwordEncoder;

    public UserFixtures(
            UserRepository users,
            RoleRepository roles,
            StudentProfileRepository studentProfiles,
            PasswordEncoder passwordEncoder) {
        this.users = users;
        this.roles = roles;
        this.studentProfiles = studentProfiles;
        this.passwordEncoder = passwordEncoder;
    }

    /** An ACTIVE user with one role, a fresh random identity, and {@link #RAW_PASSWORD}. */
    @Transactional
    public User activeUser(String roleCode) {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        User user =
                new User(
                        "fixture-" + suffix + "@test.aifu.local",
                        "fixture_" + suffix,
                        passwordEncoder.encode(RAW_PASSWORD),
                        "Fixture User " + suffix,
                        LocaleCode.DEFAULT);
        user.activate();
        Role role =
                roles.findByCode(roleCode)
                        .orElseThrow(
                                () -> new IllegalStateException("Role not seeded: " + roleCode));
        user.assignRole(role);
        return users.save(user);
    }

    /** An ACTIVE STUDENT with a {@link StudentProfile} carrying the given student number. */
    @Transactional
    public User activeStudentWithNumber(String studentNumber) {
        User user = activeUser(RoleCode.STUDENT);
        studentProfiles.save(new StudentProfile(user, studentNumber, null, null, null));
        return user;
    }
}
