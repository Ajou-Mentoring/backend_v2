package pheonix.classconnect.backend.course.entity;

import jakarta.persistence.*;
import lombok.*;
import pheonix.classconnect.backend.com.user.entity.UserEntity;

@Entity(name= "UserCourse")
@Table(name = "UserCourse")
@Getter @Setter
@Builder @AllArgsConstructor @NoArgsConstructor
public class UserCourseEntity {

    @EmbeddedId
    private UserCourseId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("courseId")
    @JoinColumn(name = "course_id")
    private CourseEntity course;

    @Column(name = "role", nullable = false)
    private Short role;
}
