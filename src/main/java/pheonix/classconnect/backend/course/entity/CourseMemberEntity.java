package pheonix.classconnect.backend.course.entity;

import jakarta.persistence.*;
import lombok.*;
import pheonix.classconnect.backend.com.user.entity.UserEntity;

@Entity(name= "CourseMember")
@Table(name = "CourseMember")
@Getter @Setter
@Builder @AllArgsConstructor @NoArgsConstructor
public class CourseMemberEntity {

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
