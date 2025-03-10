package pheonix.classconnect.backend.mentoring.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import pheonix.classconnect.backend.com.common.entity.BaseTimeEntity;
import pheonix.classconnect.backend.com.user.entity.UserEntity;
import pheonix.classconnect.backend.course.constants.CourseRole;
import pheonix.classconnect.backend.course.entity.CourseEntity;
import pheonix.classconnect.backend.mentoring.contants.MentoringStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

@Table(name = "MentoringRequest")
@Entity(name = "MentoringRequest")
@AllArgsConstructor @NoArgsConstructor
@Builder
@Getter
public class MentoringRequestEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "mentor_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private UserEntity mentor;

    @ManyToOne
    @JoinColumn(name = "requester_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private UserEntity requester;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "status")
    private Short status;

    @Column(name = "site")
    private Short site;

    @Column(name = "content", length = 600)
    private String content;

    @Column(name = "comment", length = 200)
    private String comment;

    @ManyToOne
    @JoinColumn(name = "course_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private CourseEntity course;

    @Setter
    @Column(name = "isRegistered")
    private boolean registered;

    @Type(JsonType.class)
    @Column(name = "mentees", columnDefinition = "longtext")
    private Map<String, Object> mentees;

    public void accept(String comment) {
        this.status = MentoringStatus.승인;
        this.comment = comment;
    }

    public void reject(String comment) {
        this.status = MentoringStatus.반려;
        this.comment = comment;
    }

    public void cancel(String comment, Short role) {
        this.status = role == CourseRole.MENTOR ? MentoringStatus.멘토취소 : MentoringStatus.멘티취소;
        this.comment = comment;
    }

    public void updateRequest(String content, Map<String, Object> mentees, Short site) {
        this.content = content;
        this.mentees = mentees;
        this.site = site;
    }
}
