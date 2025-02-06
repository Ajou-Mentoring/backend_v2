package pheonix.classconnect.backend.course.model.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminInviteStudentToCourseDTO {
    public List<Integer> memberIds;

    private Integer role;
}
