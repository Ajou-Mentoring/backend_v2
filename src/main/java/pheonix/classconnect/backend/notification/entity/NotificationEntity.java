package pheonix.classconnect.backend.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import pheonix.classconnect.backend.com.user.entity.UserEntity;
import pheonix.classconnect.backend.course.entity.CourseEntity;
import pheonix.classconnect.backend.notification.constants.NotificationDomain;

import java.sql.Timestamp;
import java.time.Instant;

@Entity(name = "Notification")
@Table(name = "Notification")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEntity  {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Integer id = null;

    @ManyToOne
    @JoinColumn(name= "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private CourseEntity course;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private NotificationDomain domain;

    @Column(name = "domain_id", nullable = false)
    private Integer domainId;

    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @PrePersist
    void createdAt() {
        this.createdAt = Timestamp.from(Instant.now());
    }
}
