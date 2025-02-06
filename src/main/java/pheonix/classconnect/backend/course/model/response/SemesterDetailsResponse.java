package pheonix.classconnect.backend.course.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pheonix.classconnect.backend.course.model.CourseDetail;

@AllArgsConstructor
@Getter
public class SemesterDetailsResponse {

    private String year;

    private Short semester;


    public static SemesterDetailsResponse fromCourse(CourseDetail courseDetail){
        return new SemesterDetailsResponse(courseDetail.getYear(), courseDetail.getSemester());
    }
}
