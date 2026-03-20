package co.apps.ticketing.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static co.apps.ticketing.enums.Permission.ADMIN_CREATE;
import static co.apps.ticketing.enums.Permission.ADMIN_DELETE;
import static co.apps.ticketing.enums.Permission.ADMIN_READ;
import static co.apps.ticketing.enums.Permission.ADMIN_UPDATE;
import static co.apps.ticketing.enums.Permission.MANAGER_CREATE;
import static co.apps.ticketing.enums.Permission.MANAGER_DELETE;
import static co.apps.ticketing.enums.Permission.MANAGER_READ;
import static co.apps.ticketing.enums.Permission.MANAGER_UPDATE;

@RequiredArgsConstructor
public enum Role {

    USER(Collections.emptySet()),
    ADMIN(
            Set.of(
                    ADMIN_READ,
                    ADMIN_CREATE,
                    ADMIN_UPDATE,
                    ADMIN_DELETE,
                    MANAGER_READ,
                    MANAGER_CREATE,
                    MANAGER_UPDATE,
                    MANAGER_DELETE
            )
    ),
    MANAGER(
            Set.of(
                    MANAGER_READ,
                    MANAGER_CREATE,
                    MANAGER_UPDATE,
                    MANAGER_DELETE
            )
    );

    @Getter
    private final Set<Permission> permissions;

    public List<SimpleGrantedAuthority> getAuthorities() {
        var authorities = getPermissions()
                .stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return authorities;
    }
}
