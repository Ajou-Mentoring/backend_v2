package pheonix.classconnect.backend.post.constants;

import java.util.List;

public class PostUploadStatus {
    public static Short 게시 = 1;
    public static Short 수정 = 2;
    public static Short 예약 = 3;
    public static Short 저장 = 4;
    public static Short 삭제 = 9;

    public static boolean contains(Short value) {
        return List.of(게시, 수정, 예약, 저장, 삭제).contains(value);
    }
}
