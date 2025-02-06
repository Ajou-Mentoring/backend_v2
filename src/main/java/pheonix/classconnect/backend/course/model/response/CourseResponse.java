package pheonix.classconnect.backend.course.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import pheonix.classconnect.backend.com.attachment.model.response.FileResponse;
import pheonix.classconnect.backend.course.model.CourseDTO;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
public class CourseResponse {

    private Long id;

    private ProfessorResponse professor;

    private String name;

    private String courseCode;

    private String year;

    private Short semester;

    private Integer studentCount;

    private LocalDateTime createdAt;

    private LocalDateTime  updatedAt;

    private FileResponse.Info image;


    public static CourseResponse fromCourse(CourseDTO.Course course){
        ProfessorResponse professor = ProfessorResponse.builder()
                            .id(course.getProfessor().getId())
                            .name(course.getProfessor().getName())
                            .professorNo(course.getProfessor().getProfessorNo())
                            .build();

        return new CourseResponse(course.getId(), professor, course.getName(), course.getCourseCode(), course.getYear(), course.getSemester(), course.getMembers().size(), course.getCreatedAt(), course.getUpdatedAt(), null);
    }
}
