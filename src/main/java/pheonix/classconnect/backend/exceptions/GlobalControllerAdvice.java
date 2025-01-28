package pheonix.classconnect.backend.exceptions;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pheonix.classconnect.backend.com.common.dto.Response;

@RestControllerAdvice
public class GlobalControllerAdvice {



    @ExceptionHandler(MainApplicationException.class)
    public Response<String> applicationHandler(MainApplicationException e){

        return Response.error(e.getErrorCode(), e.getMessage());
    }
}
