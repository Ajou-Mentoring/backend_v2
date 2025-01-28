package pheonix.classconnect.backend.com.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pheonix.classconnect.backend.com.user.entity.UserEntity;

import java.util.Set;

@Entity(name = "Authority")
@Table(name = "Authority")
@Getter
@NoArgsConstructor @AllArgsConstructor
public class AuthorityEntity {
    @Id
    @Column(name = "code", length = 1)
    private Short code;

    @Column(name = "kor_name", length = 15)
    private String korName;

    @Column(name = "eng_name", length = 15)
    private String engName;

    @ManyToMany(mappedBy = "authorities")
    private Set<UserEntity> owners;
}
