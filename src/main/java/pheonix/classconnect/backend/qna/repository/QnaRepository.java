package pheonix.classconnect.backend.qna.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pheonix.classconnect.backend.qna.entity.QnaEntity;

public interface QnaRepository extends JpaRepository<QnaEntity, Long> {
}
