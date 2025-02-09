package pheonix.classconnect.backend.post.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pheonix.classconnect.backend.com.common.entity.BaseTimeEntity;
import pheonix.classconnect.backend.com.user.entity.UserEntity;

@Entity(name = "Post")
@Table(name = "Post")
@AllArgsConstructor @NoArgsConstructor
@Getter
@Builder
public class PostEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_gubun")
    private Short postType;

    @Column(name = "dr_status")
    private Short uploadStatus;

    @Column(name = "publish_gubun")
    private Short publishType;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserEntity writer;
}
