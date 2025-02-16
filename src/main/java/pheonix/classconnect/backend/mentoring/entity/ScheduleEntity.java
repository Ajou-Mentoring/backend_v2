package pheonix.classconnect.backend.mentoring.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import pheonix.classconnect.backend.com.user.entity.UserEntity;
import pheonix.classconnect.backend.mentoring.repository.ScheduleRepository;

import java.time.LocalTime;

@Entity(name = "Schedule")
@Table(name = "Schedule")
@AllArgsConstructor @NoArgsConstructor
@Getter @Builder
public class ScheduleEntity {
    // PK : user_id, date, ser_no
    @EmbeddedId
    private ScheduleId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity user;

    @Column(name = "site")
    private Short site;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;
}
