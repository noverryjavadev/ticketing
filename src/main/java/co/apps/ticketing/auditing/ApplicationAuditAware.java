package co.apps.ticketing.auditing;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;
import java.util.Optional;

public class ApplicationAuditAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        try {
            String currentUser = Objects.requireNonNull(SecurityContextHolder.getContext()
                    .getAuthentication()).getName();
            return Optional.of(currentUser);
        } catch (Exception e) {
            return Optional.of("system");
        }
    }
}
