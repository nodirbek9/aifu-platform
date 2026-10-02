package uz.aifu.lms.iam.app.user;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.iam.domain.RoleCode;
import uz.aifu.lms.iam.domain.StudentProfile;
import uz.aifu.lms.iam.domain.User;
import uz.aifu.lms.iam.persistence.StudentProfileRepository;
import uz.aifu.lms.iam.persistence.UserRepository;

/**
 * Read side of "who is in which group" and people's display names, for modules that schedule or
 * report on groups (scheduling, attendance). {@code student_profiles.group_code} stays owned by
 * {@code iam}; other modules never touch that table.
 */
@Service
public class StudentDirectory {

    /** A student as other modules may show them: no email, no credentials. */
    public record StudentRef(UUID userId, String fullName, String username, String studentNumber) {}

    private final StudentProfileRepository studentProfiles;
    private final UserRepository users;

    public StudentDirectory(StudentProfileRepository studentProfiles, UserRepository users) {
        this.studentProfiles = studentProfiles;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<StudentRef> studentsOfGroup(String groupCode) {
        return studentProfiles.findByGroupCodeWithUser(groupCode).stream()
                .map(
                        p ->
                                new StudentRef(
                                        p.getUserId(),
                                        p.getUser().getFullName(),
                                        p.getUser().getUsername(),
                                        p.getStudentNumber()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Long> groupSizes(Collection<String> groupCodes) {
        if (groupCodes.isEmpty()) {
            return Map.of();
        }
        Map<String, Long> sizes = new LinkedHashMap<>();
        for (StudentProfileRepository.GroupSizeProjection row :
                studentProfiles.countByGroupCodes(groupCodes)) {
            sizes.put(row.getCode(), row.getSize());
        }
        return sizes;
    }

    @Transactional(readOnly = true)
    public Optional<String> groupCodeOf(UUID userId) {
        return studentProfiles.findById(userId).map(StudentProfile::getGroupCode);
    }

    /**
     * Puts the given students into a group ({@code null} removes them from any group).
     *
     * @return how many profiles changed
     */
    @Transactional
    public int assignGroup(Collection<UUID> userIds, String groupCode) {
        int changed = 0;
        for (UUID userId : userIds) {
            StudentProfile profile =
                    studentProfiles
                            .findById(userId)
                            .orElseThrow(() -> Exceptions.notFound("StudentProfile", userId));
            if (!java.util.Objects.equals(profile.getGroupCode(), groupCode)) {
                profile.assignGroup(groupCode);
                changed++;
            }
        }
        return changed;
    }

    @Transactional(readOnly = true)
    public Map<UUID, String> fullNames(Collection<UUID> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, String> names = new LinkedHashMap<>();
        for (User user : users.findAllById(userIds)) {
            names.put(user.getId(), user.getFullName());
        }
        return names;
    }

    @Transactional(readOnly = true)
    public boolean isTeacher(UUID userId) {
        return users.hasRole(userId, RoleCode.TEACHER);
    }

    @Transactional(readOnly = true)
    public boolean userExists(UUID userId) {
        return users.existsById(userId);
    }
}
