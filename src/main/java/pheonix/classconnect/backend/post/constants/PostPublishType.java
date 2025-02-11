package pheonix.classconnect.backend.post.constants;

import java.util.List;

public class PostPublishType {
    public static Short 전체 = 1;
    public static Short 교수 = 2;
    public static Short 관리자 = 9;

    public static boolean contains(Short value) {
        return List.of(전체, 교수, 관리자).contains(value);
    }
}
