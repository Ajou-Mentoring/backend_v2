package pheonix.classconnect.backend.course.model.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCourseDTO {

    private String name;
    private String courseCode;
}
