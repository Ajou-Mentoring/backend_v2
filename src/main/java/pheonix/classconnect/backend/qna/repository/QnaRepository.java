package pheonix.classconnect.backend.qna.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pheonix.classconnect.backend.qna.entity.QnaEntity;

public interface QnaRepository extends JpaRepository<QnaEntity, Long> {
    Page<QnaEntity> findAllByAnsweredFalse(Pageable pageable);
}
