package uz.aifu.lms.common.security;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

/** Feeds {@code @CreatedBy} / {@code @LastModifiedBy} from the authenticated caller. */
@Component
public class SpringSecurityAuditorAware implements AuditorAware<UUID> {

    private final CurrentUserProvider currentUser;

    public SpringSecurityAuditorAware(CurrentUserProvider currentUser) {
        this.currentUser = currentUser;
    }

    @Override
    public Optional<UUID> getCurrentAuditor() {
        return currentUser.find().map(AuthenticatedUser::id);
    }
}
