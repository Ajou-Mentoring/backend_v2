package pheonix.classconnect.backend.security.config;

import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import pheonix.classconnect.backend.security.interceptor.ForwardAuthenticationInterceptor;

//@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final ForwardAuthenticationInterceptor forwardAuthenticationInterceptor;

    public WebMvcConfig(ForwardAuthenticationInterceptor forwardAuthenticationInterceptor) {
        this.forwardAuthenticationInterceptor = forwardAuthenticationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(forwardAuthenticationInterceptor)
                .addPathPatterns("/**/auth/token"); // '/token' 경로에 인터셉터 적용
    }
}
