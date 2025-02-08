package pheonix.classconnect.backend.course.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor @NoArgsConstructor
public class CourseCreateRequestDTO {

    @NotNull(message = "수업 연도를 입력해주세요.")
    private String year;

    @NotNull(message = "수업 학기를 입력해주세요.")
    private Short semester;

    @NotNull(message = "과목코드를 입력해주세요.")
    private String courseCode;

    @NotNull(message = "과목이름을 입력해주세요.")
    private String name;

    @NotNull(message = "교수 이름을 입력해주세요.")
    private String professorName;
}
