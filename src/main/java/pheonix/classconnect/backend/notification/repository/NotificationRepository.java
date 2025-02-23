package pheonix.classconnect.backend.notification.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pheonix.classconnect.backend.notification.entity.NotificationEntity;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findByUserIdAndIdLessThanEqualOrderByIdDesc(Long userId, Long cursorId, Pageable pageable);
    List<NotificationEntity> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);

    Integer countByUserIdAndIsReadFalse(Long userId);


    @Modifying(clearAutomatically = true)
    @Query("update Notification n set n.isRead = true where n.user.id = :userId")
    void updateNotificationsRead(Long userId);
}
