package pheonix.classconnect.backend.course.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MentorResponse {
    private Long id;
    private String courseName;
    private String name;
    private String email;
}
