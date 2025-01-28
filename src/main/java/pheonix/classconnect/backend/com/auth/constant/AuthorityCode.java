package pheonix.classconnect.backend.com.auth.constant;

import pheonix.classconnect.backend.exceptions.ErrorCode;
import pheonix.classconnect.backend.exceptions.MainApplicationException;

public class AuthorityCode {
    /*학생*/
    public static final Short STUDENT = 1;
    /*교수*/
    public static final Short PROFESSOR = 2;
    /*관리자*/
    public static final Short ADMIN = 9;

    private AuthorityCode() {
        throw new MainApplicationException(ErrorCode.BAK_LOGIC_ERROR, "상수 클래스는 인스턴스화 할 수 없습니다.");
    }
}
