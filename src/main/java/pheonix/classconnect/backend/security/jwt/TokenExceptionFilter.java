package pheonix.classconnect.backend.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;
import pheonix.classconnect.backend.exceptions.MainApplicationException;

import java.io.IOException;

@Slf4j
public class TokenExceptionFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            filterChain.doFilter(request, response);
        } catch (MainApplicationException e) {
            log.error("토큰이 없습니다.");
        }
    }
}
