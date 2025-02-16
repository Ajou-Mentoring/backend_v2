package pheonix.classconnect.backend.mentoring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pheonix.classconnect.backend.mentoring.entity.MentoringRequestEntity;

import java.time.LocalDate;
import java.util.List;

public interface MentoringRequestRepository extends JpaRepository<MentoringRequestEntity, Long> {
    @Query(value = "SELECT * FROM MentoringRequest WHERE mentor_id = :userId AND date = :date AND status IN :statusList", nativeQuery = true)
    List<MentoringRequestEntity> findAllByUserAndDateAndStatusIn(@Param("userId") Long userId, @Param("date") LocalDate date, @Param("statusList") List<Short> statusList);
}
