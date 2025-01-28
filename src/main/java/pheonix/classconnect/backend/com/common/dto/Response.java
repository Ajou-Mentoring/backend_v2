package pheonix.classconnect.backend.com.common.dto;


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
    @Nullable
    private final T body;

    public Response(HttpStatus status, Boolean success, @Nullable T body) {
        this.status = status;
        this.body = body;
        this.success = success;
    }

    public static Response<String> error(ErrorCode errorCode, String message) {
        return new Response<>(errorCode.getHttpStatus(), false, message);
    }

    public static Response<String> ok() {
        return new Response<>(HttpStatus.OK, true, "");
    }

    public static <T> Response<T> ok(HttpStatus status, @Nullable T body) {
        return new Response<T>(status, true, body);
    }

}
