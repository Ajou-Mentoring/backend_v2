package pheonix.classconnect.backend.course.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RemoveMemberFromCourseDTO {
    public List<Long> memberIds;
}
