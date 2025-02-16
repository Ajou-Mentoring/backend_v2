package pheonix.classconnect.backend.mentoring.entity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import pheonix.classconnect.backend.com.common.entity.BaseTimeEntity;
import pheonix.classconnect.backend.com.user.entity.UserEntity;
import pheonix.classconnect.backend.course.entity.CourseEntity;

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

    @Column(name = "content")
    private String content;

    @Column(name = "comment")
    private String comment;

    @ManyToOne
    @JoinColumn(name = "course_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private CourseEntity course;

    @Type(JsonType.class)
    @Column(name = "mentees", columnDefinition = "longtext")
    private Map<String, Object> mentees;

}
