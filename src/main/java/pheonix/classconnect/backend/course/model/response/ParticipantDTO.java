package pheonix.classconnect.backend.course.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ParticipantDTO {
    private Integer id;
    private String nickname;

    private String email;

    private Integer commentCount;

    private Integer postCount;

    private Integer mentoringCount;

    private Integer messageCount;

    private Integer courseRole;
}
