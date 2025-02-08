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

    private Integer numMembers;

    private FileResponse.Info image;

    @Builder.Default
    private Short role = 0; /*클래스 역할 : 0-없음 1-멘티 2-멘토 */


    public static CourseResponse fromCourse(CourseDTO.Course course){
        return CourseResponse.builder()
                .id(course.getId())
                .professor(course.getProfessor())
                .courseCode(course.getCourseCode())
                .name(course.getName())
                .year(course.getYear())
                .semester(course.getSemester())
                .numMembers(course.getMembers().size())
                .image(null)
                .build();
    }
}
