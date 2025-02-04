package pheonix.classconnect.backend.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    BAK_LOGIC_ERROR(                HttpStatus.FORBIDDEN,                   "BAK001",    "백엔드 로직 오류입니다."),
    BAK_INVALID_PERMISSION(         HttpStatus.FORBIDDEN,                   "BAK002",   "요청 권한이 없습니다."),

    SYS_INTERNAL_SERVER_ERROR(      HttpStatus.INTERNAL_SERVER_ERROR,       "SYS500",   "서버 오류입니다."),
    SYS_UNCAUGHT_ERROR(             HttpStatus.INTERNAL_SERVER_ERROR,       "SYS999",   "핸들링 되지 않은 오류입니다."),

    DUPLICATED_USER(                HttpStatus.CONFLICT,                    "USR001" ,  "중복된 사용자입니다."),
    USER_CREATE_FAILED(              HttpStatus.INTERNAL_SERVER_ERROR,      "USR002" , "유저 생성에 실패했습니다."),

    AUTH_NOT_FOUND(                 HttpStatus.NOT_FOUND,                   "ATH001",   "권한을 찾을 수 없습니다.")

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}
