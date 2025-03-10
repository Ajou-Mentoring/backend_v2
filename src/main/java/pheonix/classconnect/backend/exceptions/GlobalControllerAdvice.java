package pheonix.classconnect.backend.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pheonix.classconnect.backend.com.common.model.Response;

@Slf4j
@RestControllerAdvice
public class GlobalControllerAdvice {



    @ExceptionHandler(MainApplicationException.class)
    public Response<String> applicationHandler(MainApplicationException e){
        log.error("[{}]{}",e.getErrorCode().getCode(), e.getMessage());
        return Response.error(e.getErrorCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response<String> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        log.error(e.getMessage());

        return Response.error(ErrorCode.BACK_INVALID_PARAMETER, "API 파라미터 에러입니다." + e.getMessage());
    }


    @ExceptionHandler(Exception.class)
    public Response<String> unhandledExceptionHandler(Exception e) {
        log.error("{} => {}", e.getClass(), e.getMessage());
        return Response.error(ErrorCode.SYS_UNCAUGHT_ERROR, "확인되지 않은 에러입니다. IT 담당자에게 문의하세요.");
    }
}
