package pheonix.classconnect.backend.notification.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pheonix.classconnect.backend.course.model.CourseDTO;
import pheonix.classconnect.backend.notification.entity.NotificationEntity;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private Long id;
    private Long courseId;
    private String content;
    private Boolean isRead;
    private Integer domain;
    private Long domainId;

    private LocalDate createdDate;

    private LocalTime createdTime;


    public static NotificationDTO fromEntity(NotificationEntity entity){
        return new NotificationDTO(entity.getId(),
                entity.getCourse() != null ? entity.getCourse().getId(): null,
                entity.getContent(),
                entity.getIsRead(),
                entity.getDomain().getCode(),
                entity.getDomainId(),
                entity.getCreatedDate(),
                entity.getCreatedTime()
        );
    }
}
