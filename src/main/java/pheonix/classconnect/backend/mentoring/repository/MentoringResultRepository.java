package pheonix.classconnect.backend.mentoring.repository;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import pheonix.classconnect.backend.mentoring.entity.MentoringResultEntity;

import java.time.LocalDate;
import java.util.List;

public interface MentoringResultRepository extends JpaRepository<MentoringResultEntity, Long> {
    List<MentoringResultEntity> findAllByCourseIdAndMentorIdAndDateBetweenOrderByDateAscTimeAsc(Long courseId, Long mentorId, LocalDate from, LocalDate to);

    List<MentoringResultEntity> findAllByMentorIdOrderByDateAscTimeAsc(@NotNull Long mentorId);
}
