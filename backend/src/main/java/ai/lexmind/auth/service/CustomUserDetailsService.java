package ai.lexmind.auth.service;

import ai.lexmind.auth.domain.Permission;
import ai.lexmind.auth.domain.User;
import ai.lexmind.auth.domain.UserStatus;
import ai.lexmind.auth.repo.UserRepository;
import ai.lexmind.common.security.UserPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailWithRole(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return toPrincipal(user);
    }

    /** Build the stateless principal (with permission authorities) from a persisted user. */
    public static UserPrincipal toPrincipal(User user) {
        List<String> permissionCodes = user.getRole().getPermissions().stream()
                .map(Permission::getCode)
                .toList();
        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getOrganizationId(),
                user.getRole().getName(),
                user.getStatus() == UserStatus.ACTIVE,
                permissionCodes);
    }
}
