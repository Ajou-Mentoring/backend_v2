package pheonix.classconnect.backend.qna.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pheonix.classconnect.backend.qna.entity.QnaEntity;

import java.util.Optional;

public interface QnaRepository extends JpaRepository<QnaEntity, Long> {
    Page<QnaEntity> findAllByAnsweredFalse(Pageable pageable);

    @Query("SELECT q FROM QNA q " +
            "JOIN FETCH q.questioner qu " +
            "JOIN FETCH qu.department " +
            "JOIN FETCH qu.authorities " +
            "LEFT JOIN FETCH q.answerer ans " +
            "LEFT JOIN FETCH ans.department " +
            "LEFT JOIN FETCH ans.authorities " +
            "WHERE q.id = :id")
    Optional<QnaEntity> findByIdWithUsers(@Param("id") Long id);




}
