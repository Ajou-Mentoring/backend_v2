package pheonix.classconnect.backend.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {

        log.info(request.getHeader("Authorization"));

        // Todo: 필요한 경우에만 JWT 검증하는 조건문 추가
        String jwt = jwtTokenProvider.resolveToken(request);
        String requestURI = request.getRequestURI();

        if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)){
            Authentication authentication = jwtTokenProvider.getAuthentication(jwt);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            Integer userId = jwtTokenProvider.getUserIdFromJwt(jwt);
            log.info("Security Context에 'UID = {}' 인증 정보를 저장했습니다, uri: {}", userId, requestURI);
        } else {
            log.info("유효한 JWT 토큰이 없습니다, uri: {}", requestURI);
        }

        filterChain.doFilter(request, response);
        // 다음 filter를 실행하거나, 마지막 필터라면 실행 후 resource 반환
    }
}
