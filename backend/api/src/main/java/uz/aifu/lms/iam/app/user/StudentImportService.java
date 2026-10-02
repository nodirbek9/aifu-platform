package uz.aifu.lms.iam.app.user;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import uz.aifu.lms.common.error.DomainException;
import uz.aifu.lms.iam.api.dto.UserDtos.CreateUserRequest;
import uz.aifu.lms.iam.api.dto.UserDtos.ImportStudentResult;
import uz.aifu.lms.iam.api.dto.UserDtos.ImportStudentRow;
import uz.aifu.lms.iam.api.dto.UserDtos.ImportStudentsRequest;
import uz.aifu.lms.iam.api.dto.UserDtos.StudentProfilePayload;
import uz.aifu.lms.iam.domain.RoleCode;
import uz.aifu.lms.iam.domain.StudentProfile;
import uz.aifu.lms.iam.persistence.StudentProfileRepository;
import uz.aifu.lms.iam.persistence.UserRepository;

/**
 * Imports a list of students with their groups (product spec A-05). The browser parses the
 * registrar's CSV or Excel file and sends the rows; this service decides, row by row, what would
 * happen ({@code dryRun}) or does it.
 *
 * <p>A student number that already exists is not an error: that student is moved to the row's group
 * when it differs — which makes the same import the way to assign groups in bulk. Each row stands
 * alone: one bad row never undoes the others (deliberately not one transaction).
 */
@Service
public class StudentImportService {

    public static final String CREATED = "CREATED";
    public static final String MOVED = "MOVED";
    public static final String UNCHANGED = "UNCHANGED";
    public static final String FAILED = "FAILED";

    private final UserService users;
    private final StudentDirectory directory;
    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfiles;

    public StudentImportService(
            UserService users,
            StudentDirectory directory,
            UserRepository userRepository,
            StudentProfileRepository studentProfiles) {
        this.users = users;
        this.directory = directory;
        this.userRepository = userRepository;
        this.studentProfiles = studentProfiles;
    }

    public List<ImportStudentResult> importStudents(
            ImportStudentsRequest request, UUID actorId, String ip) {
        boolean dryRun = Boolean.TRUE.equals(request.dryRun());
        List<ImportStudentResult> results = new ArrayList<>();
        Set<String> seenNumbers = new HashSet<>();
        Set<String> seenUsernames = new HashSet<>();
        Set<String> seenEmails = new HashSet<>();
        int rowNo = 0;
        for (ImportStudentRow row : request.rows()) {
            rowNo++;
            String number = trim(row.studentNumber());
            String fullName = trim(row.fullName());
            String group = trim(row.groupCode());
            if (number == null || fullName == null) {
                results.add(failed(rowNo, number, null, "fullName and studentNumber are required"));
                continue;
            }
            if (!seenNumbers.add(number.toLowerCase(Locale.ROOT))) {
                results.add(
                        failed(rowNo, number, null, "This student number is in the file twice"));
                continue;
            }

            Optional<StudentProfile> existing = studentProfiles.findByStudentNumber(number);
            if (existing.isPresent()) {
                UUID userId = existing.get().getUserId();
                if (Objects.equals(existing.get().getGroupCode(), group) || group == null) {
                    results.add(
                            new ImportStudentResult(rowNo, number, null, UNCHANGED, userId, null));
                } else {
                    if (!dryRun) {
                        directory.assignGroup(List.of(userId), group);
                    }
                    results.add(new ImportStudentResult(rowNo, number, null, MOVED, userId, null));
                }
                continue;
            }

            String username = trim(row.username());
            if (username == null) {
                username = usernameFrom(number);
            }
            String email = trim(row.email());
            if (email == null
                    && request.emailDomain() != null
                    && !request.emailDomain().isBlank()) {
                email = username + "@" + request.emailDomain().trim();
            }
            String problem = problemWith(username, email, request, seenUsernames, seenEmails);
            if (problem != null) {
                results.add(failed(rowNo, number, username, problem));
                continue;
            }
            if (dryRun) {
                results.add(new ImportStudentResult(rowNo, number, username, CREATED, null, null));
                continue;
            }
            try {
                UUID userId =
                        users.create(
                                        new CreateUserRequest(
                                                email,
                                                username,
                                                fullName,
                                                request.initialPassword(),
                                                request.preferredLocale(),
                                                trim(row.phone()),
                                                Set.of(RoleCode.STUDENT),
                                                true,
                                                new StudentProfilePayload(
                                                        number,
                                                        row.programmeId(),
                                                        group,
                                                        row.enrollmentYear()),
                                                null),
                                        actorId,
                                        ip)
                                .getId();
                results.add(
                        new ImportStudentResult(rowNo, number, username, CREATED, userId, null));
            } catch (DomainException e) {
                results.add(failed(rowNo, number, username, e.getMessage()));
            }
        }
        return results;
    }

    private String problemWith(
            String username,
            String email,
            ImportStudentsRequest request,
            Set<String> seenUsernames,
            Set<String> seenEmails) {
        if (!username.matches("^[a-z0-9._-]{3,64}$")) {
            return "The username must be 3-64 characters of a-z, 0-9, dot, dash or underscore";
        }
        if (email == null) {
            return "No email in the row and no emailDomain to build one from";
        }
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return "The email address is not valid";
        }
        if (request.initialPassword() == null) {
            return "initialPassword is required to create accounts";
        }
        if (!seenUsernames.add(username) || userRepository.existsByUsernameIgnoreCase(username)) {
            return "Username is already taken";
        }
        if (!seenEmails.add(email.toLowerCase(Locale.ROOT))
                || userRepository.existsByEmailIgnoreCase(email)) {
            return "Email is already registered";
        }
        return null;
    }

    /** A student number as a login name: lowercased, anything unusual replaced by a dash. */
    static String usernameFrom(String studentNumber) {
        String base = studentNumber.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "-");
        return base.length() >= 3 ? base : "s-" + base;
    }

    private static ImportStudentResult failed(int row, String number, String username, String why) {
        return new ImportStudentResult(row, number, username, FAILED, null, why);
    }

    private static String trim(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
