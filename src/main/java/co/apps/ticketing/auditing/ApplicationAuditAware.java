package co.apps.ticketing.auditing;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;
import java.util.Optional;

public class ApplicationAuditAware implements AuditorAware<String> {
//    @Override
//    public Optional<Integer> getCurrentAuditor() {
//        Authentication authentication =
//                SecurityContextHolder
//                        .getContext()
//                        .getAuthentication();
//        if (authentication == null ||
//            !authentication.isAuthenticated() ||
//                authentication instanceof AnonymousAuthenticationToken
//        ) {
//            return Optional.empty();
//        }
//
//        User userPrincipal = (User) authentication.getPrincipal();
//        return Optional.ofNullable(userPrincipal.getId());
//    }

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
