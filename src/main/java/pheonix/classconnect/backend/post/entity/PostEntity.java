package pheonix.classconnect.backend.post.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
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

    @Column(name = "title", length = 60)
    private String title;

    @Column(name = "content", length = 1000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity writer;

    public void updatePost(Short postType, Short uploadStatus, Short publishType, String title, String content) {
        this.postType = postType;
        this.uploadStatus = uploadStatus;
        this.publishType = publishType;
        this.title = title;
        this.content = content;
    }
}
