package pheonix.classconnect.backend.security.utils;

import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import pheonix.classconnect.backend.exceptions.ErrorCode;
import pheonix.classconnect.backend.exceptions.MainApplicationException;

@Component
public class PrincipalUtils {
    public static Long getUserId(User user) {
        if (user == null) {
            throw new MainApplicationException(ErrorCode.BACK_INVALID_PERMISSION, "인증객체에서 유저 정보를 찾을 수 없습니다.");
        }
        return Long.parseLong(user.getUsername());
    }
}
