package pheonix.classconnect.backend.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pheonix.classconnect.backend.com.user.entity.UserEntity;
import pheonix.classconnect.backend.com.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class PrincipalDetailsService implements UserDetailsService {
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserRepository userEntityRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("JWT 유저 로딩 : {}", username);
        return userEntityRepository.findById(Long.parseLong(username))
                .map(this::createUser)
                .orElseThrow(() -> new UsernameNotFoundException("로컬 DB에 등록되지 않은 사용자입니다."));
    }

    public User createUser(UserEntity user) {
        log.info("UserDetails 생성");

        Set<GrantedAuthority> authorities = user.getAuthorities().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_".concat(role.getEngName())))
                .collect(Collectors.toSet());

        return new User(user.getId().toString(), passwordEncoder.encode("1234"), authorities);
    }

    public User createGuest(String guestId) {
        GrantedAuthority guestRole = new SimpleGrantedAuthority("ROLE_GUEST");
        return new User(guestId, passwordEncoder.encode("1234"), Set.of(guestRole));
    }
}
