package pheonix.classconnect.backend.com.common.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.lang.Nullable;
import pheonix.classconnect.backend.exceptions.ErrorCode;

@Getter
@AllArgsConstructor
public class Response<T> {
    private final HttpStatusCode status;
    private final Boolean success;
    private final String message;
    @Nullable
    private final T result;


    public Response(HttpStatus status, Boolean success, String message, @Nullable T result) {
        this.status = status;
        this.success = success;
        this.message = message;
        this.result = result;
    }

    public static Response<String> error(ErrorCode errorCode, String message) {
        return new Response<>(errorCode.getHttpStatus(), false, message, null);
    }

    public static Response<String> ok() {
        return new Response<>(HttpStatus.OK, true, "요청 성공!", null);
    }

    public static Response<String> ok(String message) {
        return new Response<>(HttpStatus.OK, true, message, null);
    }

    public static <T> Response<T> ok(HttpStatus status, @Nullable String message, @Nullable T body) {
        return new Response<T>(status, true, message, body);
    }

}
