package pheonix.classconnect.backend.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pheonix.classconnect.backend.post.entity.PostEntity;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
}
