package pheonix.classconnect.backend.exceptions;


import lombok.Getter;

public class MainApplicationException extends RuntimeException {
    @Getter
    private final ErrorCode errorCode;
    private final String cause;

    public MainApplicationException(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.cause = "";
    }

    public MainApplicationException(ErrorCode errorCode, String cause) {
        this.errorCode = errorCode;
        this.cause = cause;
    }

    @Override
    public String getMessage() {
        return String.format("<%s> %s => %s", errorCode.getCode(), errorCode.getMessage(), cause);
    }

}
