package pheonix.classconnect.backend.course.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class CourseFetchRequestDTO {

    @NotNull(message = "수업 연도를 입력해주세요.")
    private String year;

    @NotNull(message = "수업 학기를 입력해주세요.")
    private Short semester;

    private Long userId;

}
