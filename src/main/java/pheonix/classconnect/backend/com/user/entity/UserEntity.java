package pheonix.classconnect.backend.com.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import pheonix.classconnect.backend.com.auth.entity.AuthorityEntity;
import pheonix.classconnect.backend.com.common.entity.BaseTimeEntity;
import pheonix.classconnect.backend.com.department.entity.DepartmentEntity;
import pheonix.classconnect.backend.notification.entity.NotificationEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity(name = "User")
@Table(name = "User")
@Getter
@Builder
@AllArgsConstructor @NoArgsConstructor
public class UserEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", length = 9, unique = true)
    private String studentNo;

    @Column(name = "email", length = 50, unique = true)
    private String email;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "active_state", length = 1)
    private Short activeState;

    @ManyToOne(targetEntity = DepartmentEntity.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name="dep_id", referencedColumnName = "id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private DepartmentEntity department;

    @ManyToMany
    @JoinTable(
            name = "UserAuth",
            joinColumns = {
                    @JoinColumn(name = "user_id", referencedColumnName = "id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "auth_code", referencedColumnName = "code")
            }
    )
    Set<AuthorityEntity> authorities;


//    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
//    private NotificationSettingEntity notificationSetting = new NotificationSettingEntity(this);

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<NotificationEntity> notifications = new ArrayList<>();

    public void updateDepartment(DepartmentEntity department) {
        this.department = department;
    }

    public void addAuthority(AuthorityEntity auth) {
        if (this.authorities == null || this.authorities.isEmpty()) {
            this.authorities = Set.of(auth);
        }
        else this.authorities.add(auth);
    }

    public void updateInfo(String name) {
        this.name = name;
    }

}
