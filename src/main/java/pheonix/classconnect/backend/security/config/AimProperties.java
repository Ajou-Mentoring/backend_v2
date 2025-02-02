package pheonix.classconnect.backend.security.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Getter @Setter
@Validated
@ConfigurationProperties(prefix = "aim", ignoreUnknownFields = false)
public class AimProperties {
    private Saml saml;

    @Getter @Setter
    public static class Saml {
        private String idpMetadataPath;
        private String acsUrl;
        private String spEntityId;
        private String returnUrl;
        private char[] sharedSecret;
    }

}
