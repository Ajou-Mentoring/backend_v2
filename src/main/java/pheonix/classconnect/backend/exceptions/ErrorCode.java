package pheonix.classconnect.backend.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    BAK_LOGIC_ERROR(                HttpStatus.INTERNAL_SERVER_ERROR,       "BAK001",   "백엔드 로직 오류입니다."),
    SYS_INTERNAL_SERVER_ERROR(      HttpStatus.INTERNAL_SERVER_ERROR,       "SYS500",   "서버 오류입니다."),
    SYS_UNCAUGHT_ERROR(             HttpStatus.INTERNAL_SERVER_ERROR,       "SYS999",   "핸들링 되지 않은 오류입니다.")
    ;

    private HttpStatus httpStatus;
    private String code;
    private String message;

}
