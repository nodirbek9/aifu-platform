package uz.aifu.lms.common.security;

import java.util.Set;
import java.util.UUID;

/**
 * The authenticated caller, built from the access token. Immutable and Spring-free so services can
 * depend on it without dragging in web types.
 */
public record AuthenticatedUser(
        UUID id, String username, Set<String> roles, Set<String> permissions) {

    public boolean isAdmin() {
        return roles.contains("ADMIN");
    }

    public boolean isTeacher() {
        return roles.contains("TEACHER");
    }

    public boolean isStudent() {
        return roles.contains("STUDENT");
    }

    public boolean has(String permission) {
        return permissions.contains(permission);
    }
}
