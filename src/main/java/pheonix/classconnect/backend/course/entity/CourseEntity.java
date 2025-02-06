package pheonix.classconnect.backend.course.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import pheonix.classconnect.backend.com.common.entity.BaseTimeEntity;
import pheonix.classconnect.backend.course.constants.Semester;

import java.util.ArrayList;
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

    //TODO:  ERD 확정 및 테이블 생성된 이후에 연관관계 주입
    @ManyToOne
    @JoinColumn(name="professor_id")
    private ProfessorEntity professor;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "code", length = 6)
    private String code;

    @Column(name = "open_year", length = 4)
    private String year;

    @Column(name = "open_semester")
    private Short semester;

    @Column(name = "open_status")
    private Short status;

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    private List<UserCourseEntity> students;

    @Column(name = "invitation_code", length = 6)
    private String invitationCode;

//    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
//    @OnDelete(action = OnDeleteAction.CASCADE)
//    private List<MentoringEntity> mentorings = new ArrayList<>();

//    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
//    @OnDelete(action = OnDeleteAction.CASCADE)
//    private List<NotificationEntity> notifications = new ArrayList<>();

    public void allocateProfessor(ProfessorEntity professor) {
        this.professor = professor;
    }

    public void changeInvitationCode(String code) {
        this.invitationCode = code;
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
