package pheonix.classconnect.backend.course.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class CourseFetchRequestDTO {

    private String year;

    private Short semester;

    private Long memberId;

}
