package pheonix.classconnect.backend.mentoring.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import pheonix.classconnect.backend.com.user.entity.UserEntity;
import pheonix.classconnect.backend.course.entity.CourseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

@Entity(name = "MentoringResult")
@Table(name = "MentoringResult")
@AllArgsConstructor @NoArgsConstructor
@Getter
@Builder
public class MentoringResultEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mentoring_date")
    private LocalDate date;

    @Column(name = "mentoring_time")
    private LocalTime time;

    @Column(name = "length")
    private Integer length;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "content", length = 400)
    private String content;

    @Type(JsonType.class)
    @Column(name = "mentees", columnDefinition = "longtext")
    private Map<String, Object> mentees;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", referencedColumnName = "id")
    @OnDelete(action = OnDeleteAction.RESTRICT)
    private CourseEntity course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", referencedColumnName = "id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private UserEntity mentor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", referencedColumnName = "id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private MentoringRequestEntity request;

    public void updateResult(LocalDate date, LocalTime time, Integer length, String location, String content, Map<String, Object> mentees) {
        this.date = date;
        this.time = time;
        this.length = length;
        this.location = location;
        this.content = content;
        this.mentees = mentees;
    }
}
