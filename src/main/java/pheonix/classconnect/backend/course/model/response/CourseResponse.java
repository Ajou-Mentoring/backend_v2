package pheonix.classconnect.backend.course.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import pheonix.classconnect.backend.com.attachment.model.response.FileResponse;
import pheonix.classconnect.backend.course.model.CourseDTO;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class CourseResponse {

    private Long id;

    private String professor;

    private String name;

    private String courseCode;

    private String year;

    private Short semester;

    private Integer studentCount;

    private LocalDateTime createdAt;

    private LocalDateTime  updatedAt;

    private FileResponse.Info image;


    public static CourseResponse fromCourse(CourseDTO.Course course){
        return CourseResponse.builder()
                .id(course.getId())
                .professor(course.getProfessor())
                .name(course.getName())
                .year(course.getYear())
                .semester(course.getSemester())
                .studentCount(course.getMembers().size())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .image(null)
                .build();
    }
}
