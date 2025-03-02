package pheonix.classconnect.backend.mentoring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pheonix.classconnect.backend.mentoring.entity.MentoringRequestEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MentoringRequestRepository extends JpaRepository<MentoringRequestEntity, Long> {
    @Query(value = "SELECT * FROM MentoringRequest WHERE mentor_id = :userId AND date = :date AND status IN :statusList ORDER BY date ASC, start_time asc", nativeQuery = true)
    List<MentoringRequestEntity> findAllByUserAndDateAndStatusIn(@Param("userId") Long userId, @Param("date") LocalDate date, @Param("statusList") List<Short> statusList);

    List<MentoringRequestEntity> findAllByMentorIdAndCourseIdAndDateBetweenOrderByDateAscStartTimeAsc(Long mentorId, Long courseId, LocalDate start, LocalDate end);

    List<MentoringRequestEntity> findAllByRequesterIdAndCourseIdAndDateBetweenOrderByDateAscStartTimeAsc(Long requesterId, Long courseId, LocalDate start, LocalDate end);

    List<MentoringRequestEntity> findAllByCourseIdAndDateBetweenAndStatusOrderByDateAscStartTimeAsc(Long courseId, LocalDate start, LocalDate end, Short status);

    Optional<MentoringRequestEntity> findTopByMentorIdAndRequesterIdOrderByIdDesc(Long mentorId, Long requesterId);

    List<MentoringRequestEntity> findByStatusAndDate(short status, LocalDate date);
}
