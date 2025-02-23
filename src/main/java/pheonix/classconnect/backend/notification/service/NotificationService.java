package pheonix.classconnect.backend.notification.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pheonix.classconnect.backend.com.common.model.PageResponse;
import pheonix.classconnect.backend.com.user.service.UserService;
import pheonix.classconnect.backend.exceptions.ErrorCode;
import pheonix.classconnect.backend.exceptions.MainApplicationException;
import pheonix.classconnect.backend.notification.entity.NotificationEntity;
import pheonix.classconnect.backend.notification.model.NotificationDTO;
import pheonix.classconnect.backend.notification.repository.NotificationRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;


    @Autowired
    private UserService userService;

    @PersistenceContext
    private EntityManager entityManager;



//    private NotificationEntity buildNotification(User user, Course course, String content, NotificationDomain domain, Integer domainId) {
//        // UserEntity의 PK로 프록시 객체 가져옴(쿼리x)
//        UserEntity userEntity = entityManager.getReference(UserEntity.class, user.getId());
//        // CourseEntity의 PK로 프록시 객체 가져옴(쿼리x)
//        CourseEntity courseEntity = entityManager.getReference(CourseEntity.class, course.getId());
//
//        return NotificationEntity.builder()
//                .user(userEntity)
//                .course(courseEntity)
//                .content(content)
//                .isRead(false)
//                .domain(domain)
//                .domainId(domainId)
//                .createdAt(new Timestamp(System.currentTimeMillis()))
//                .build();
//    }

    public PageResponse<NotificationDTO> getMyNotifications(Long userId, pheonix.classconnect.backend.com.common.model.PageRequest dto) {
        Integer pageSize = dto.getSize();
        Long cursorId = dto.getCursorId();

        Pageable pageable = PageRequest.of(dto.getPage(), pageSize + 1, Sort.by(Sort.Direction.DESC, "id"));

        List<NotificationEntity> notificationEntities = cursorId == null ?
                notificationRepository.findByUserIdOrderByIdDesc(userId, pageable) :
                notificationRepository.findByUserIdAndIdLessThanEqualOrderByIdDesc(userId, cursorId, pageable);

        List<NotificationDTO> notifications = notificationEntities.stream()
                .map(NotificationDTO::fromEntity)
                .collect(Collectors.toList());

        PageResponse<NotificationDTO> response = new PageResponse<>();
        boolean hasNext = notifications.size() > pageSize;


        response.setItems(notifications);
        response.setHasNext(hasNext);

        // 다음 아이템이 없다면 -> 마지막 페이지라면
        if (!hasNext) {
            response.setNextCursorId(null);
        }

        // 다음 아이템들이 있다면 -> 마지막 페이지가 아니라면
        else{
            response.setNextCursorId(notifications.get(notifications.size() - 1).getId());
            notifications.remove(notifications.size() - 1);
        }

        return response;
    }

    public Integer getMyUnreadNotificationsCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void updateIsRead(Long notificationId, Long userId) {
        NotificationEntity notificationEntity = notificationRepository.findById(notificationId).orElseThrow(
                () -> new MainApplicationException(ErrorCode.NOTIFICATION_NOT_FOUND)
        );

        if(!notificationEntity.getUser().getId().equals(userId)){
            throw new MainApplicationException(ErrorCode.NOTIFICATION_NOT_AUTHORIZED);
        }

        notificationEntity.setIsRead(Boolean.TRUE);
        notificationRepository.save(notificationEntity);
    }

    @Transactional
    public void updateAllNotificationsRead(Long userId) {
        notificationRepository.updateNotificationsRead(userId);
    }
}