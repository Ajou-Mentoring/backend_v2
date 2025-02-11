package pheonix.classconnect.backend.post.constants;

import java.util.List;

public class PostType {
    public static Short 공지사항 = 1;
    public static Short 업데이트 = 2;

    public static boolean contains(Short value) {
        return List.of(공지사항, 업데이트).contains(value);
    }
}
