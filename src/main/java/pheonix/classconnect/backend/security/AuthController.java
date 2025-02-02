package pheonix.classconnect.backend.security;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;
import pheonix.classconnect.backend.com.user.model.UserDTO;
import pheonix.classconnect.backend.com.user.service.UserService;
import pheonix.classconnect.backend.exceptions.ErrorCode;
import pheonix.classconnect.backend.exceptions.MainApplicationException;
import pheonix.classconnect.backend.security.jwt.JwtTokenProvider;
import pheonix.classconnect.backend.security.service.AuthService;
import pheonix.classconnect.backend.security.service.PrincipalDetailsService;

import java.net.URI;
import java.util.List;

@Slf4j
@RequestMapping("/v2/auth")
@Controller
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final AuthService authService;
    private final PrincipalDetailsService principalDetailsService;
    private final RestTemplate restTemplate;
    private final static String CLASS_NAME = "AuthController";
    @Value("${spring.security.client.landing-page-url}")
    private String homePageUrl;
    @Value("${spring.security.client.sign-up-page-url}")
    private String signupPageUrl;
    @Value("${server.domain")
    private String serverDomain;
    @Value("${server.port-export")
    private String serverPort;


    @RequestMapping("/token")
    public String issueToken(
            RedirectAttributes redirect,
            @RequestParam("userId") String studentCode) {
        log.info("{}.issueToken()", CLASS_NAME);
        String token = null;

        // 사용자 등록여부 체크
        UserDTO.FindOne findDto = UserDTO.FindOne.builder().studentNo(studentCode).build();
        UserDTO.UserInfo userInfo = userService.findOne(findDto);

        // 등록되지 않은 사용자의 경우 학번으로 토큰 발급
        if (userInfo == null) {
            log.debug("신규 사용자 => 학번으로 토큰 발급");
            token = authService.issueToken(principalDetailsService.createGuest(studentCode));
        }
        else {
            log.debug("유저 ID로 토큰 발급");
            token = authService.issueToken(principalDetailsService.loadUserByUsername(userInfo.getId().toString()));
        }

        redirect.addAttribute("token", token);
        redirect.addAttribute("signup", userInfo == null);

        return "redirect:/v2/auth/redirect";
    }

    @GetMapping("/redirect")
    public String redirectAfterLogin(
            @RequestParam(value = "token", required = false) String token,
            @RequestParam(value = "signup", required = false) boolean needSignup,
                RedirectAttributes redirect
    ) {
        log.info("{}.redirectAfterLogin()", CLASS_NAME);

        // 회원가입이 필요할 경우 회원 가입 페이지로 리디렉션 / 아니면 홈으로 리디렉션
        String landingPageUrl = determineLandingPageUrl(needSignup);

        try {
            UriComponentsBuilder ucb = UriComponentsBuilder
                    .fromHttpUrl(landingPageUrl);

            HttpHeaders httpHeaders = new HttpHeaders();

            // token이 발급되었다면 Authorization에 토큰 주입
            if (token != null && !token.isEmpty()) {
                httpHeaders.add("Authorization", "Bearer " + token);
                ucb.queryParam("token", token);
            }

            URI destination = ucb.build().toUri();
            httpHeaders.setLocation(destination);
            redirect.addAttribute("token", token);

            return needSignup ? "redirect:" + signupPageUrl : "redirect:" + homePageUrl;
        }
        catch (Exception e) {
            log.error("Redirection failed!");
        }
        throw new MainApplicationException(ErrorCode.SYS_INTERNAL_SERVER_ERROR, "인증 후 리디렉션에 실패했습니다. 다시 시도해주세요.");
    }

    @PostMapping("/sign-up")
    public String signup(
            @RequestBody @Valid UserDTO.Signup signupDTO,
            @AuthenticationPrincipal User user,
            Model model) {
        log.info("{}.singup()", CLASS_NAME);

        // 요청자와 가입자가 동일한지 검사
        if (!signupDTO.getStudentNo().equals(user.getUsername()))
            throw new MainApplicationException(ErrorCode.BAK_INVALID_PERMISSION, String.format("생성할 유저 정보와 요청자 정보가 다릅니다. => 가입자[%s] 요청자[%s]", signupDTO.getStudentNo(), user.getUsername()));

        // 이미 가입된 유저인지 검사
        if (userService.findOne(UserDTO.FindOne.builder().studentNo(signupDTO.getStudentNo()).build()) != null) {
            throw new MainApplicationException(ErrorCode.DUPLICATED_USER, "이미 가입된 유저입니다.");
        }

        postNewUserBySignupDto(signupDTO);

        // 토큰 발급 페이지로 포워딩
        model.addAttribute("message", "회원가입 성공!");
        model.addAttribute("redirectUrl", String.format("%s:%s%s", serverDomain, serverPort, "/api/v1/auth/token?userId=" + signupDTO.getStudentNo()));
        return "alert2";
    }

    private String determineLandingPageUrl(boolean needSignup) {
        return needSignup ? signupPageUrl : homePageUrl;

    }

    private void postNewUserBySignupDto(UserDTO.Signup signupDTO) {
        // 타겟 URL 설정
        String targetUrl = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host("api.class-connect.kr")
                .port(serverPort)
                .path("/api/v2/users/sign-up")
                .toUriString();
        log.info("URL = {}", targetUrl);
        // HTTP 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        // 요청 바디와 헤더를 포함한 HttpEntity 객체 생성
        HttpEntity<UserDTO.Signup> request = new HttpEntity<>(signupDTO, headers);

        try {
            restTemplate.exchange(
                    targetUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );
        } catch (RestClientException e) {
            log.error("유저 등록에 실패했습니다.");
            e.printStackTrace();
        }
    }
}
