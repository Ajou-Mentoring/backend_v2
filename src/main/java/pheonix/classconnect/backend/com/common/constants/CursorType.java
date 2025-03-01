package pheonix.classconnect.backend.com.common.constants;

public enum CursorType {
    FIXED("fixed"),

    NORMAL("normal");

    private String value;
    CursorType(String value){
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}
