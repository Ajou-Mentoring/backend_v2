package pheonix.classconnect.backend.notification.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pheonix.classconnect.backend.com.common.model.PageResponse;
import pheonix.classconnect.backend.com.user.entity.UserEntity;
import pheonix.classconnect.backend.course.entity.CourseMemberEntity;
import pheonix.classconnect.backend.course.model.CourseDTO;
import pheonix.classconnect.backend.course.repository.CourseMemberEntityRepository;
import pheonix.classconnect.backend.exceptions.ErrorCode;
import pheonix.classconnect.backend.exceptions.MainApplicationException;
import pheonix.classconnect.backend.mentoring.contants.MentoringStatus;
import pheonix.classconnect.backend.mentoring.entity.MentoringRequestEntity;
import pheonix.classconnect.backend.mentoring.entity.TimeTableEntity;
import pheonix.classconnect.backend.mentoring.repository.MentoringRequestRepository;
import pheonix.classconnect.backend.mentoring.repository.TimeTableRepository;
import pheonix.classconnect.backend.notification.constants.NotificationDomain;
import pheonix.classconnect.backend.notification.entity.NotificationEntity;
import pheonix.classconnect.backend.notification.model.NotificationDTO;
import pheonix.classconnect.backend.notification.repository.NotificationRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private MentoringRequestRepository mentoringRequestRepository;

    @Autowired
    private CourseMemberEntityRepository courseMemberEntityRepository;

    @Autowired
    private TimeTableRepository  timeTableRepository;


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

    public PageResponse<NotificationDTO.SimplifiedNotification> getMyNotifications(Long userId, pheonix.classconnect.backend.com.common.model.PageRequest dto) {
        Integer pageSize = dto.getSize();
        Long cursorId = dto.getCursorId();

        Pageable pageable = PageRequest.of(0, pageSize + 1, Sort.by(Sort.Direction.DESC, "id"));

        List<NotificationEntity> notificationEntities = cursorId.equals(0L) ?
                notificationRepository.findByUserIdOrderByIdDesc(userId, pageable) :
                notificationRepository.findByUserIdAndIdLessThanEqualOrderByIdDesc(userId, cursorId, pageable);

        List<NotificationDTO.SimplifiedNotification> notifications = new ArrayList<>();

        for (NotificationEntity entity : notificationEntities) {
            Long courseId = (entity.getCourse() != null) ? entity.getCourse().getId() : null;
            String courseName = (entity.getCourse() != null) ? entity.getCourse().getName() : null;

            Short courseRole = (courseId != null)
                    ? courseMemberEntityRepository.findByUserIdAndCourseId(userId, courseId)
                    .map(CourseMemberEntity::getRole)
                    .orElse(null)
                    : null;


            notifications.add(NotificationDTO.SimplifiedNotification.builder()
                    .id(entity.getId())
                    .courseId(courseId)
                    .courseName(courseName)
                    .courseRole(courseRole)
                    .content(entity.getContent())
                    .isRead(entity.getIsRead())
                    .domain(entity.getDomain().getCode())
                    .domainId(entity.getDomainId())
                    .createdDate(entity.getCreatedDate())
                    .createdTime(entity.getCreatedTime())
                    .build());
        }




        PageResponse<NotificationDTO.SimplifiedNotification> response = new PageResponse<>();
        boolean hasNext = notifications.size() > pageSize;

        if (hasNext) {
            response.setNextCursorId(notifications.get(notifications.size() - 1).getId());
            notifications.remove(notifications.size() - 1);
        }

        response.setItems(notifications);
        response.setHasNext(hasNext);

        if (!hasNext) {
            response.setNextCursorId(null);
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

    @Transactional
    public void createNotification(NotificationEntity notificationEntity) {
        notificationRepository.save(notificationEntity);
    }

    @Transactional
    public void createMentoringNotifications() {
        System.out.println("실행됨1");
        // 내일 진행될 mentoringRequest 목록 조회
        LocalDate tomorrow = LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(1);

        List<MentoringRequestEntity> requests = mentoringRequestRepository.findByStatusAndDate(MentoringStatus.승인, tomorrow);

        List<NotificationEntity> notifications = new ArrayList<>();

        for (MentoringRequestEntity request : requests) {
            String content = "멘토링이 예정되어 있습니다. 시간: "
                    + request.getStartTime() + " - " + request.getEndTime();


            if (request.getMentor() != null && request.getRequester() != null) {
                UserEntity mentor = request.getMentor();
                UserEntity mentee = request.getRequester();
                notifications.add(buildNotification(mentor, request, mentee.getName()+"님과 " +content));
                notifications.add(buildNotification(mentee, request, mentor.getName()+"님과 " + content));
            }
        }

        // 알림 저장
        notificationRepository.saveAll(notifications);
    }

    @Transactional
    public void createTimeTableNotifications() {
        System.out.println("실행됨");
        // 오늘 기준 1일, 3일, 7일 후 날짜 계산
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        LocalDate oneDayBefore = today.plusDays(1);
        LocalDate threeDaysBefore = today.plusDays(3);
        LocalDate sevenDaysBefore = today.plusDays(7);

        // 각 날짜에 해당하는 유저별 최근 endDate 조회
        List<TimeTableEntity> oneDayList = timeTableRepository.findLatestEndDateUsers(oneDayBefore);
        List<TimeTableEntity> threeDayList = timeTableRepository.findLatestEndDateUsers(threeDaysBefore);
        List<TimeTableEntity> sevenDayList = timeTableRepository.findLatestEndDateUsers(sevenDaysBefore);

        // 알림 생성
        List<NotificationEntity> notifications = new ArrayList<>();
        notifications.addAll( createTimeTableNotifications(oneDayList, 1));
        notifications.addAll( createTimeTableNotifications(threeDayList, 3));
        notifications.addAll( createTimeTableNotifications(sevenDayList, 7));

        // DB 저장
        notificationRepository.saveAll(notifications);
    }

    private List<NotificationEntity> createTimeTableNotifications(List<TimeTableEntity> timeTableList, int daysBefore) {
        List<NotificationEntity> notifications = new ArrayList<>();
        for (TimeTableEntity timeTable : timeTableList) {
            String content = "주간 일정 등록 종료 일자가 " + daysBefore + "일 전입니다. 주간 일정을 갱신해주세요.";

            notifications.add(NotificationEntity.builder()
                    .user(timeTable.getUser())
                    .course(null)
                    .content(content)
                    .isRead(false)
                    .domain(NotificationDomain.WEEKLY_TIMETABLE)
                    .domainId(null)
                    .createdDate(LocalDate.now())
                    .createdTime(LocalTime.now())
                    .build());
        }
        return notifications;
    }

    private NotificationEntity buildNotification(UserEntity user, MentoringRequestEntity request, String content) {
        return NotificationEntity.builder()
                .user(user)
                .course(request.getCourse())
                .content(content)
                .isRead(false)
                .domain(NotificationDomain.MENTORING)
                .domainId(request.getId())
                .createdDate(LocalDate.now())
                .createdTime(LocalTime.now())
                .build();
    }
}