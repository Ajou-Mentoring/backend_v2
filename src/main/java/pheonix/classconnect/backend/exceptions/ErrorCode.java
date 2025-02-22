package pheonix.classconnect.backend.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    BAK_LOGIC_ERROR(                HttpStatus.FORBIDDEN,                   "BACK001",    "백엔드 로직 오류입니다."),
    BACK_INVALID_PERMISSION(         HttpStatus.UNAUTHORIZED,                   "BACK002",   "요청 권한이 없습니다."),
    BACK_NONNULL_PARAMETER(         HttpStatus.BAD_REQUEST,                 "BACK003",      "파라미터가 Null입니다."),

    SYS_INTERNAL_SERVER_ERROR(      HttpStatus.INTERNAL_SERVER_ERROR,       "SYS500",   "서버 오류입니다."),
    SYS_UNCAUGHT_ERROR(             HttpStatus.INTERNAL_SERVER_ERROR,       "SYS999",   "핸들링 되지 않은 오류입니다."),

    DUPLICATED_USER(                HttpStatus.BAD_REQUEST,                    "USER001" ,  "중복된 사용자입니다."),
    USER_CREATE_FAILED(              HttpStatus.INTERNAL_SERVER_ERROR,      "USER002" , "유저 생성에 실패했습니다."),
    USER_NOT_FOUND(                 HttpStatus.NOT_FOUND,                   "USER003",      "유저를 찾을 수 없습니다."),
    AUTH_NOT_FOUND(                 HttpStatus.NOT_FOUND,                   "AUTH001",   "권한을 찾을 수 없습니다."),

    UNSUPPORTED_FILE(HttpStatus.BAD_REQUEST, "FILE001", "지원하지 않는 파일 구분입니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE002","파일을 찾을 수 없습니다." ),

    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE001", "코스를 찾을 수 없습니다."),
    DUPLICATED_COURSE(HttpStatus.BAD_REQUEST, "COURSE002", "중복된 코스입니다."),
    COURSE_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE003", "코스 멤버 정보가 없습니다."),
    DUPLICATED_COURSE_MEMBER(HttpStatus.BAD_REQUEST, "COURSE004", "코스 멤버 중복입니다."),
    COURSE_NOT_OPEN(HttpStatus.BAD_REQUEST, "COURSE005", "코스가 개설 상태가 아닙니다."),
    COURSE_INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "COURSE006", "파라미터 오류입니다."),

    POST_INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "POST001", "지원하지 않는 파라미터입니다."),
    POST_NOT_FOUND(HttpStatus.BAD_REQUEST, "POST002", "게시물을 찾을 수 없습니다."),
    POST_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "POST003", "게시물 접근 권한이 없습니다."),
    QNA_NOT_FOUND(HttpStatus.NOT_FOUND, "QNA001", "Q&A 게시물을 찾을 수 없습니다."),
    QNA_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "QNA002", "Q&A 게시물 접근 권한이 없습니다."),

    MENTOR_TIME_CONFLICT(HttpStatus.CONFLICT, "MENTOR409", "멘토링 스케줄 중복입니다."),
    MENTOR_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "MENTOR001", "멘토링 접근 권한이 없습니다."),
    MENTORING_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "MENTOR002", "멘토링 요청 정보를 찾을 수 없습니다."),
    MENTORING_INVALID_STATUS_CHANGE(HttpStatus.BAD_REQUEST, "MENTOR003", "멘토링 상태 변경 오류입니다."),
    MENTORING_REQUEST_PARAMETER_NULL(HttpStatus.BAD_REQUEST, "MENTOR004", "멘토링 필수 파라미터 누락입니다."), 
    MENTORING_REQUEST_INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "MENTOR005", "멘토링 파라미터 오류입니다."),
    MENTORING_REQUEST_FORBIDDEN_REQUEST(HttpStatus.FORBIDDEN, "MENTOR006", "지원하지 않는 멘토링 요청입니다."),
    MENTORING_REQUEST_ERROR(HttpStatus.BAD_REQUEST, "MENTOR007", "멘토링 신청 오류입니다."),
    MENTEE_NOT_FOUND(HttpStatus.NOT_FOUND, "MENTOR008", "멘티를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}
