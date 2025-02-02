package pheonix.classconnect.backend.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * JWT Access 토큰, Refresh 토큰 발급
 * JWT -> Authentication 객체 추출
 * JWT 유효성 검사
 */
@Slf4j
@Component
public class JwtTokenProvider implements InitializingBean {
    private Key key;  // 암호화 후
    private final String secret; // 암호화 전
    private final long tokenValidityInMilliSeconds;
    private static final String AUTHORITIES_KEY = "auth";
    private final String header;
    private final String prefix;

    private static JwtParser jwtParser;

    public JwtTokenProvider(
            @Value("${jwt.prefix}") String prefix,
            @Value("${jwt.header}") String header,
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expired-at}") long expTime) {

        this.prefix = prefix;
        this.header = header;
        this.secret = secret;
        this.tokenValidityInMilliSeconds = expTime;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        byte[] keyBytes = Decoders.BASE64.decode(secret);  // secret을 key로 JWT 서명
        this.key = Keys.hmacShaKeyFor(keyBytes);
        jwtParser = Jwts.parserBuilder().setSigningKey(key).build();
    }

    public String createToken(Authentication authentication) {
        log.info("토큰 발급");
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date expiredAt = new Date(now + this.tokenValidityInMilliSeconds);
        log.info("subject 주입 : {}", authentication.getName());

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .setIssuedAt(new Date())
                .setExpiration(expiredAt)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    // JWT 토큰에서 인증 정보 조회 -> Authentication 객체 반환
    public Authentication getAuthentication(String accessToken) {
        log.info("Token에서 권한 추출 : from {}", accessToken);
        Claims claims = jwtParser.parseClaimsJws(accessToken).getBody();

        if (claims.get(AUTHORITIES_KEY) == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // 클레임에서 권한 정보 가져오기
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
        User principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    // JWT 토큰에서 유저 아이디 추출
    public Integer getUserIdFromJwt(String jwtToken) {
        log.info("토큰에서 USER ID 추출");

        Claims claims = jwtParser.parseClaimsJws(jwtToken).getBody();

        log.info("user id = {}", claims.getSubject());
        return Integer.parseInt(claims.getSubject());
    }

    // 토큰의 유효성 + 만료일자 확인
    public boolean validateToken(String authToken) {
        log.info("토큰 유효성 검사");
        try {
            jwtParser.parseClaimsJws(authToken);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 비어있습니다.");
        }
        return false;
    }

    // Request 헤더에서 token 추출
    public String resolveToken(HttpServletRequest request)
    {
        log.info("토큰 추출");
        String bearerToken = request.getHeader(header);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(prefix)) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
