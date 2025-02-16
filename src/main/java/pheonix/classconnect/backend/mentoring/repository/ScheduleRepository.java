package pheonix.classconnect.backend.mentoring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pheonix.classconnect.backend.mentoring.entity.ScheduleEntity;
import pheonix.classconnect.backend.mentoring.entity.ScheduleId;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<ScheduleEntity, ScheduleId> {

    @Query(value = "SELECT MAX(serNo) FROM Schedule WHERE user_id = :userId AND date = :date", nativeQuery = true)
    Integer findMaxSerNoByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    List<ScheduleEntity> findAllById_UserIdAndId_DateOrderByStartTime(Long userId, LocalDate date);

    List<ScheduleEntity> findAllById_UserIdAndId_DateBetweenOrderById_DateAscStartTimeAsc(Long userId, LocalDate startDate, LocalDate endDate);

    void deleteAllById_UserIdAndIdDate(Long userId, LocalDate date);
}
