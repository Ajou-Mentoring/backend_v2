package pheonix.classconnect.backend.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pheonix.classconnect.backend.security.jwt.JwtAuthenticationFilter;
import pheonix.classconnect.backend.security.jwt.JwtTokenProvider;

// TokenProvider를 통해 전달받은 토큰의 유효성을 검증하는 필터를 Security 로직에 등록한다.
@Configuration
@RequiredArgsConstructor
public class JwtSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {
    private JwtTokenProvider tokenProvider;

    public JwtSecurityConfig(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public void configure(HttpSecurity http) {
        JwtAuthenticationFilter customFilter = new JwtAuthenticationFilter(tokenProvider);
        http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
