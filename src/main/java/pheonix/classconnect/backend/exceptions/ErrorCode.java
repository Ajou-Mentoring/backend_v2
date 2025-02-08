package pheonix.classconnect.backend.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    BAK_LOGIC_ERROR(                HttpStatus.FORBIDDEN,                   "BACK001",    "백엔드 로직 오류입니다."),
    BAK_INVALID_PERMISSION(         HttpStatus.FORBIDDEN,                   "BACK002",   "요청 권한이 없습니다."),

    SYS_INTERNAL_SERVER_ERROR(      HttpStatus.INTERNAL_SERVER_ERROR,       "SYS500",   "서버 오류입니다."),
    SYS_UNCAUGHT_ERROR(             HttpStatus.INTERNAL_SERVER_ERROR,       "SYS999",   "핸들링 되지 않은 오류입니다."),

    DUPLICATED_USER(                HttpStatus.CONFLICT,                    "USER001" ,  "중복된 사용자입니다."),
    USER_CREATE_FAILED(              HttpStatus.INTERNAL_SERVER_ERROR,      "USER002" , "유저 생성에 실패했습니다."),
    USER_NOT_FOUND(                 HttpStatus.NOT_FOUND,                   "USER003",      "유저를 찾을 수 없습니다."),

    AUTH_NOT_FOUND(                 HttpStatus.NOT_FOUND,                   "AUTH001",   "권한을 찾을 수 없습니다."),

    UNSUPPORTED_FILE(HttpStatus.BAD_REQUEST, "FILE001", "지원하지 않는 파일 구분입니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE002","파일을 찾을 수 없습니다." ),
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE001", "코스를 찾을 수 없습니다."),
    DUPLICATED_COURSE(HttpStatus.CONFLICT, "COURSE002", "중복된 코스입니다."),
    COURSE_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE003", "코스 멤버 정보가 없습니다."),
    DUPLICATED_COURSE_MEMBER(HttpStatus.FORBIDDEN, "COURSE004", "코스 멤버 중복입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}
