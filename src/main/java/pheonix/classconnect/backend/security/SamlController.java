package pheonix.classconnect.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import pheonix.classconnect.backend.exceptions.MainApplicationException;
import pheonix.classconnect.backend.security.config.AimProperties;
import pheonix.classconnect.backend.security.service.SamlClient;
import pheonix.classconnect.backend.security.service.SamlResponse;
import pheonix.classconnect.backend.security.utils.BrowserUtils;
import pheonix.classconnect.backend.security.utils.CommonUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RequestMapping("/v2/saml")
@Controller
public class SamlController {
    private final String serverDomain;
    private final String serverPort;
    private final AimProperties properties;
    private final SamlClient client;

    public SamlController(final AimProperties properties,
                          @Value("${server.domain}") String serverDomain,
                          @Value("${server.port}") String serverPort
    ) throws IOException, MainApplicationException {

        this.properties = properties;
        this.serverDomain = serverDomain;
        this.serverPort = serverPort;

        String idpMetadataPath = this.properties.getSaml().getIdpMetadataPath();
        ClassPathResource classPathResource = new ClassPathResource(idpMetadataPath);
        Reader metadata = new InputStreamReader(classPathResource.getInputStream());
        String spEntityId = this.properties.getSaml().getSpEntityId();
        String acsUrl = String.format("%s:%s%s", serverDomain, serverPort, this.properties.getSaml().getAcsUrl());
        log.info("acs-url==============={}", acsUrl);
        client = SamlClient.fromMetadata(spEntityId, acsUrl, metadata, "POST");
    }

    @GetMapping(value="/login")
    public ModelAndView loginSAML(
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) throws MainApplicationException, IOException {

        int phase = 1;
        UUID seq = UUID.randomUUID();

        // 사용자 ip address
        String ipAddress = CommonUtils.getRealRemoteIp(servletRequest);
        HttpSession session = servletRequest.getSession(true);
        if (session != null) {
            log.info("loginSAML-> ipAddress={}, set seq to {}", ipAddress, seq);
            session.setAttribute("Seq", seq);
        } else {
            log.error("loginSAML-> ipAddress={}, session is null", ipAddress);
        }

        log.info("\n\tloginSAML-> ipAddress={}, Seq={}, Phase={}", ipAddress, seq, phase);

        client.redirectToIdentityProvider(servletResponse);
        return null;
    }

    @PostMapping(value="/acs")
    public ModelAndView acs(
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse,
            @RequestParam(value="RelayState", required=false)String paramRelayState)
            throws IOException, MainApplicationException {

        int phase = 2;
        UUID seq = null;

        // 사용자 ip address
        String ipAddress = CommonUtils.getRealRemoteIp(servletRequest);

        HttpSession session = servletRequest.getSession();
        if (session != null)
        {
            seq = (UUID)session.getAttribute("Seq");
        }

        else
            log.error("acs-> ipAddress={}, session is null", ipAddress);

        SamlResponse response = client.processPostFromIdentityProvider(servletRequest);

        // 사용자 정보를 가져온다.
        String userNo = response.getNameID();

        log.info(
                "\n\tacs-> ipAddress={}, Seq={}, Phase={}, userNo={}\n" +
                        "encoded SAMLResponse={}\n" +
                        "decoded SAMLResponse={}\n",
                ipAddress, seq, phase, userNo,
                response.getEncodedResponse(),
                response.getDecodedResponse());

        // 1. session 을 클리어한다.
        if (session != null) session.invalidate();

        // 2. 사용자 인증 정보를 담아 넘겨준다.
        char[] sharedSecret = this.properties.getSaml().getSharedSecret();
        Map<String, String> authMap = CommonUtils.genAuthMap(sharedSecret, userNo);

        String returnUrl = String.format("%s:%s%s", serverDomain, serverPort, this.properties.getSaml().getReturnUrl());
        BrowserUtils.getUsingBrowser(returnUrl, servletResponse, authMap);
        return null;
    }

    @GetMapping("/login-success")
    public String processAfterLoginSuccess(
            @RequestParam("auth") String auth,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("userId") String userId,
            Model model
    ) throws MainApplicationException, IOException {

        // 요청 유효성 검사
        char[] sharedSecret = this.properties.getSaml().getSharedSecret();
        String encodedValue = CommonUtils.genAuthMap(sharedSecret, userId).get("auth");
        if (!encodedValue.equals(auth)) {
            log.error("유효하지 않은 인증 요청");

            model.addAttribute("message", "유효하지 않은 인증 요청입니다.");
            model.addAttribute("redirectUrl", "login");
            return "alert";
        }

        log.info("SSO 로그인 성공");

        // 로그인 성공시 토큰 발급 Controller로 Redirection
        return "forward:/v2/auth/token";
    }

}
