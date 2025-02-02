package pheonix.classconnect.backend.security.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.*;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import pheonix.classconnect.backend.security.jwt.*;
import pheonix.classconnect.backend.security.service.PrincipalDetailsService;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final PrincipalDetailsService principalDetailsService;
    private final JwtTokenProvider tokenProvider;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/favicon.io/**");
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://localhost:3000", "http://localhost:8080", "https://localhost:8080", "https://class-connect.kr", "https://sso.ajou.ac.kr", "https://api.class-connect.kr", "https://test.class-connect.kr:8443", "null"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "X-Requested-With", "Access-Control-Request-Method", "Access-Control-Request-Headers", "Access-Control-Allow-Origin", "Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Basic
                .httpBasic(HttpBasicConfigurer::disable)
                .cors(httpSecurityCorsConfigurer -> corsConfigurationSource())
                .csrf(CsrfConfigurer::disable) // csrf 비활성화 -> token 방식 적용
                .formLogin(FormLoginConfigurer::disable)  // form 로그인 비활성화
                // 로그아웃 설정
                .logout(config -> config
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID"))

                .headers(c -> c.frameOptions(
                        HeadersConfigurer.FrameOptionsConfig::disable).disable())
                .rememberMe(RememberMeConfigurer::disable) // rememberMe 비활성화
                .sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // JWT Token으로 대체
                // 인증, 인가
                .authorizeHttpRequests(request ->  // HttpServletRequest 를 사용하는 모든 요청에 대한 접근 제한 설정
                        request
                                .requestMatchers("/api/v2/*/login/*", "/api/v2/test/*").permitAll()
                                .requestMatchers("/api/v2/admin/**").hasRole("ADMIN")
                                .requestMatchers("/api/v2/**").authenticated()// Todo: Role에 따른 접근 권한 설정
                                .anyRequest().permitAll()
                )

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)  // JWT 필터 추가
                .addFilterBefore(new TokenExceptionFilter(), JwtAuthenticationFilter.class)

                .userDetailsService(principalDetailsService)
                .exceptionHandling(exception ->
                        exception
                                .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                                .accessDeniedHandler(new JwtAccessDeniedHandler()))




                // filter 설정

                // HTTPS 적용 설정
//                .requiresChannel(channel ->
//                        channel
//                                .anyRequest().requiresSecure())
        ;

        return http.build();
    }
}
