package pheonix.classconnect.backend.mentoring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pheonix.classconnect.backend.mentoring.entity.TimeTableEntity;
import pheonix.classconnect.backend.mentoring.entity.TimeTableId;

import java.util.List;

public interface TimeTableRepository extends JpaRepository<TimeTableEntity, TimeTableId> {

    @Query(value = "SELECT MAX(ver) FROM TimeTable WHERE user_id = :userId", nativeQuery = true)
    Integer findMaxVerByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT MAX(serNo) FROM TimeTable WHERE user_id = :userId AND ver = :version", nativeQuery = true)
    Integer findMaxSerNoByUserIdVer(@Param("userId") Long userId, @Param("version") Integer version);

    List<TimeTableEntity> findAllById_UserIdAndId_Ver(Long userId, Integer version);
}
