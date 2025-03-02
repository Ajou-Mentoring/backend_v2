package pheonix.classconnect.backend.post.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pheonix.classconnect.backend.post.entity.PostEntity;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    Page<PostEntity> findAllByPostTypeLessThanEqualAndUploadStatusLessThanEqualAndPublishTypeLessThanEqual(Short postType, Short uploadStatus, Short publishType, Pageable pageable);
}
