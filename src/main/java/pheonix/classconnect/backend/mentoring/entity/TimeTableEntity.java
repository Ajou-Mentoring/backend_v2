package pheonix.classconnect.backend.mentoring.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import pheonix.classconnect.backend.com.user.entity.UserEntity;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity(name = "TimeTable")
@Table(name = "TimeTable")
@NoArgsConstructor @AllArgsConstructor
@Getter
@Builder
public class TimeTableEntity {
    // PK : user_id, version, ser_no
    @EmbeddedId
    private TimeTableId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity user;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "day")
    private Short day;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "site")
    private Short site;

}
