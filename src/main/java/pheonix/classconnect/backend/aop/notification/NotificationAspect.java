package pheonix.classconnect.backend.aop.notification;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pheonix.classconnect.backend.com.user.entity.UserEntity;
import pheonix.classconnect.backend.com.user.model.UserDTO;
import pheonix.classconnect.backend.com.user.service.UserService;
import pheonix.classconnect.backend.course.constants.CourseRole;
import pheonix.classconnect.backend.course.entity.CourseEntity;
import pheonix.classconnect.backend.course.model.CourseDTO;
import pheonix.classconnect.backend.course.service.CourseMemberService;
import pheonix.classconnect.backend.course.service.CourseService;
import pheonix.classconnect.backend.mentoring.entity.MentoringRequestEntity;
import pheonix.classconnect.backend.mentoring.model.MentoringRequestDTO;
import pheonix.classconnect.backend.mentoring.service.MentoringService;
import pheonix.classconnect.backend.notification.constants.NotificationDomain;
import pheonix.classconnect.backend.notification.entity.NotificationEntity;
import pheonix.classconnect.backend.notification.service.NotificationService;





@Aspect
@Component
@Async
public class NotificationAspect {

    @Autowired
    private NotificationService notificationService;


    @Autowired
    private CourseMemberService courseMemberService;


    @Autowired
    private MentoringService mentoringService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;
    @PersistenceContext
    private EntityManager entityManager;





    /**
     * Aop PointCut 메서드
     * 멘토링 신청 후 실행되는 메서드
     * @param dto 멘토링 요청 DTO
     */
    @Pointcut("execution(* pheonix.classconnect.backend.mentoring.service.MentoringService.createRequest(..)) && args(dto)")
    public void afterMentoringRequest(MentoringRequestDTO.Create dto){}

    /**
     * Aop Advice 메서드
     * 멘토링 신청 후 실행되는 메서드
     *
     * @param dto 멘토링 요청 DTO
     * Description: 멘토에게 멘토링 신청이 왔다는 알림과 메일을 보내는 메서드
     */
    @AfterReturning(value="afterMentoringRequest(dto)", returning = "dto")
    private void afterMentoringCreate(MentoringRequestDTO.Create dto) {
        // 멘토링 요청 조회
        MentoringRequestEntity mentoringRequest =
                mentoringService.getMentoringRequestByMentorAndMentee(dto.getMentorId(), dto.getRequesterId());

        UserEntity mentor = mentoringRequest.getMentor();
        CourseEntity courseEntity = mentoringRequest.getCourse();

        String content = String.format("%s 수업의 %s 학생이 %s %s~%s로 멘토링을 신청했습니다.",
                mentoringRequest.getCourse().getName(), mentoringRequest.getRequester().getName(), dto.getDate(), dto.getStartTime(), dto.getEndTime());

        NotificationEntity notificationEntity = NotificationEntity.builder()
                .user(mentor)
                .course(courseEntity)
                .content(content)
                .domain(NotificationDomain.MENTORING)
                .domainId(mentoringRequest.getId())
                .isRead(false)
                .build();

        notificationService.createNotification(notificationEntity);

        //TODO: 메일 개발이후 해당 로직 구현
//    mailService.sendMentoringMail(dto);
    }



    @AfterReturning(value="execution(* pheonix.classconnect.backend.mentoring.service.MentoringService.acceptRequest(..)) && args(requestId, comment)")
    public void afterAcceptMentoring(Long requestId, String comment) {
        sendMentoringStatusNotification(requestId, "승인", comment);
    }

    @AfterReturning(value="execution(* pheonix.classconnect.backend.mentoring.service.MentoringService.rejectRequest(..)) && args(requestId, comment)")
    public void afterRejectMentoring(Long requestId, String comment) {
        sendMentoringStatusNotification(requestId, "반려", comment);
    }


    //TODO: 누가 취소했는지에 대한 로그가 필요함 알림이 ~~멘티가 취소했습니다 혹은 ~~멘토가 취소했습니다. 이런식으로 구분이 되어야함
//    @AfterReturning(value="execution(* pheonix.classconnect.backend.mentoring.service.MentoringService.cancelRequest(..)) && args(requestId, comment)")
//    public void afterCancelMentoring(Long requestId, String comment) {
//        sendMentoringStatusNotification(requestId, "취소", comment);
//    }

    private void sendMentoringStatusNotification(Long requestId, String status, String comment) {
        // 멘토링 요청 조회
        MentoringRequestEntity mentoringRequestEntity = mentoringService.getMentoringRequestEntityById(requestId);

        UserEntity mentor = mentoringRequestEntity.getMentor();
        CourseEntity course= mentoringRequestEntity.getCourse();
        UserEntity mentee = mentoringRequestEntity.getRequester();

        // 알림 메시지 생성
        String content = String.format("%s 수업의 %s 멘토가 멘토링 요청을 %s했습니다. 사유: %s",
                course.getName(), mentor.getName(), status, comment);

        NotificationEntity notificationEntity = NotificationEntity.builder()
                .user(mentee)
                .course(course)
                .content(content)
                .domainId(mentoringRequestEntity.getId())
                .isRead(false)
                .build();

        // 알림 생성
        notificationService.createNotification(notificationEntity);
    }


    @AfterReturning(value="execution(* pheonix.classconnect.backend.course.service.CourseMemberService.updateMemberRoleInCourse(..)) && args(courseId, userId, role)")
    public void afterCourseRoleChanged(Long courseId, Long userId, Short role) {
        sendNotificationAfterCourseRoleChanged(courseId, userId, role);
    }

    private void sendNotificationAfterCourseRoleChanged(Long courseId, Long userId, Short role) {
        // 강의 및 사용자 조회
        CourseDTO.Course course = courseService.getACourseById(courseId);
        UserDTO.User  user = userService.findUserById(userId);

        // 역할에 따른 메시지 설정
        String roleName;

        if (role.equals(CourseRole.MENTEE)) {
            roleName = "멘티";
        } else if (role.equals(CourseRole.MENTOR)) {
            roleName = "멘토";
        } else if (role.equals(CourseRole.PROFESSOR)) {
            roleName = "교수";
        } else {
            return ;
        }


        String content = String.format("%s님이 %s 수업에서 %s 역할로 변경되었습니다.",
                user.getName(), course.getName(), roleName);


        UserEntity userEntity = entityManager.getReference(UserEntity.class, user.getId());
        CourseEntity courseEntity = entityManager.getReference(CourseEntity.class, course.getId());

        NotificationEntity notificationEntity = NotificationEntity.builder()
                .user(userEntity)
                .course(courseEntity)
                .content(content)
                .domainId(courseEntity.getId())
                .isRead(false)
                .build();


        // 알림 생성
        notificationService.createNotification(notificationEntity);
    }

}

