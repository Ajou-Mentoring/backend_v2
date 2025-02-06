package pheonix.classconnect.backend.course.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfessorResponse {
    private Long id;
    private String name;
    private String professorNo;
}
