package pheonix.classconnect.backend.course.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import pheonix.classconnect.backend.com.common.entity.BaseTimeEntity;

import java.util.List;

@Entity(name= "Course")
@Table(name = "Course")
@Getter @Setter
@Builder @AllArgsConstructor @NoArgsConstructor
@SQLDelete(sql = "UPDATE Course SET deleted_at = NOW() where id = ?")
@Where(clause = "deleted_at is NULL")
public class CourseEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "code", length = 6)
    private String code;

    @Column(name = "open_year", length = 4)
    private String year;

    @Column(name = "open_semester")
    private Short semester;

    // 교수 명은 텍스트로 - 임시 기능
    @Column(name = "professor", length = 50)
    private String professor;

    @Column(name = "open_status")
    private Short status;

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    private List<CourseMemberEntity> members;

    @Column(name = "member_code", length = 6)
    private String memberCode;

//    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
//    @OnDelete(action = OnDeleteAction.CASCADE)
//    private List<MentoringEntity> mentorings = new ArrayList<>();

//    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
//    @OnDelete(action = OnDeleteAction.CASCADE)
//    private List<NotificationEntity> notifications = new ArrayList<>();

    public void changeInvitationCode(String code) {
        this.memberCode = code;
    }

    @Override
    public String toString() {
        return "CourseEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                '}';
    }
}
