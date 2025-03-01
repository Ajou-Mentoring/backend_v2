package pheonix.classconnect.backend.aop.notification.enums;

public enum NotificationDomain {
    COURSE(0),
    POST(1),
    MESSAGE(2),
    MENTORING(3),

    COMMENT(4);

    private final int code;

    NotificationDomain(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
