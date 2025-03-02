package pheonix.classconnect.backend.security.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.WebUtils;
import pheonix.classconnect.backend.exceptions.ErrorCode;
import pheonix.classconnect.backend.exceptions.MainApplicationException;

//@Component
public class ForwardAuthenticationInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 요청이 FORWARD를 통해 들어왔는지 확인
        if (request.getAttribute(WebUtils.FORWARD_REQUEST_URI_ATTRIBUTE) == null) {
            throw new MainApplicationException(ErrorCode.BAK_LOGIC_ERROR, "해당 요청은 Forward 방식만 접근 가능합니다.");
        }
        return true;
    }
}
