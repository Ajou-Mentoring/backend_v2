package pheonix.classconnect.backend.com.user.constant;

import pheonix.classconnect.backend.exceptions.ErrorCode;
import pheonix.classconnect.backend.exceptions.MainApplicationException;

public class UserActiveStatus {
    /*활동*/
    public static final Short ACTIVE = 1;

    /*미승인*/
    public static final Short PENDING = 2;

    /*미가입*/
    public static final Short WAIT = 3;

    /*휴면*/
    public static final Short INACTIVE = 8;

    /*탈퇴*/
    public static final Short OUT = 9;

    private UserActiveStatus() {
        // TODO: 에러 클래스 정의하기
        throw new MainApplicationException(ErrorCode.BAK_LOGIC_ERROR, "상수 클래스는 인스턴스화 할 수 없습니다.");
    }
}
