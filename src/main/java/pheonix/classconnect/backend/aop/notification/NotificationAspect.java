package pheonix.classconnect.backend.aop.notification;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pheonix.classconnect.backend.com.auth.model.AuthorityDTO;
import pheonix.classconnect.backend.com.user.entity.UserEntity;
import pheonix.classconnect.backend.com.user.service.UserService;
import pheonix.classconnect.backend.course.constants.CourseRole;
import pheonix.classconnect.backend.course.entity.CourseEntity;
import pheonix.classconnect.backend.course.service.CourseService;
import pheonix.classconnect.backend.mentoring.contants.MentoringStatus;
import pheonix.classconnect.backend.mentoring.entity.MentoringRequestEntity;
import pheonix.classconnect.backend.mentoring.model.MentoringRequestDTO;
import pheonix.classconnect.backend.mentoring.service.MentoringService;
import pheonix.classconnect.backend.notification.constants.NotificationDomain;
import pheonix.classconnect.backend.notification.entity.NotificationEntity;
import pheonix.classconnect.backend.notification.service.NotificationService;
import pheonix.classconnect.backend.qna.model.QnaDTO;
import pheonix.classconnect.backend.qna.service.QnaService;


@Aspect
@Component
@Transactional
public class NotificationAspect {

    @Autowired
    private NotificationService notificationService;



    @Autowired
    private MentoringService mentoringService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    @Autowired
    private QnaService qnaService;

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
    @AfterReturning(value = "afterMentoringRequest(dto)")
    @Async("customThreadPool")
    public void afterMentoringCreate(MentoringRequestDTO.Create dto) {


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



    @Pointcut("execution(* pheonix.classconnect.backend.mentoring.service.MentoringService.acceptRequest(..)) && args(requestId, comment)")
    public void acceptMentoringRequest(Long requestId, String comment) {}

    @Pointcut("execution(* pheonix.classconnect.backend.mentoring.service.MentoringService.rejectRequest(..)) && args(requestId, comment)")
    public void rejectMentoringRequest(Long requestId, String comment) {}

    @Pointcut("execution(* pheonix.classconnect.backend.mentoring.service.MentoringService.cancelRequest(..)) && args(requestId, courseId, comment, userId)")
    public void cancelMentoringRequest(Long requestId, Long courseId, String comment, Long userId) {}

    @Pointcut("execution(* pheonix.classconnect.backend.course.service.CourseMemberService.updateMemberRoleInCourse(..)) && args(courseId, userId, role)")
    public void courseRoleChanged(Long courseId, Long userId, Short role) {}

    @Pointcut("execution(* pheonix.classconnect.backend.qna.service.QnaService.createAnswer(..)) && args(dto)")
    public void qnaAnswerCreated(QnaDTO.Answer dto){}



    @AfterReturning(value = "acceptMentoringRequest(requestId, comment)")
    @Async("customThreadPool")
    public void afterAcceptMentoring(Long requestId, String comment) {
        sendMentoringStatusNotification(requestId, "승인", comment);
    }

    @AfterReturning(value = "rejectMentoringRequest(requestId, comment)")
    @Async("customThreadPool")
    public void afterRejectMentoring(Long requestId, String comment) {
        sendMentoringStatusNotification(requestId, "반려", comment);
    }

     @AfterReturning(value = "cancelMentoringRequest(requestId,courseId,comment, userId)")
     @Async("customThreadPool")
     public void afterCancelMentoring(Long requestId, Long courseId, String comment, Long userId) {
         sendMentoringStatusNotification(requestId, "취소", comment);
     }

    @AfterReturning(value = "courseRoleChanged(courseId, userId, role)")
    @Async("customThreadPool")
    public void afterCourseRoleChanged(Long courseId, Long userId, Short role) {
        sendNotificationAfterCourseRoleChanged(courseId, userId, role);
    }

    @Transactional(readOnly = true)
    @Async("customThreadPool")
    @AfterReturning(value = "qnaAnswerCreated(dto)")
    public void afterQnaAnswerCreated(QnaDTO.Answer dto){
        QnaDTO.Qna qna = qnaService.getQnaAndUsersByQnaId(dto.getId());
        Long questionerId = qna.getQuestioner().getId();

        boolean isOnlyStudent = qna.getAnswerer().getAuthorities().stream()
                .map(AuthorityDTO.AuthorityInfo::getCode)
                .anyMatch(code -> code == 9);


        //관리자라면
        if(!isOnlyStudent){
            return;
        }

        String content =  qna.getTitle()+ "  문의의 답변이 작성되었습니다.";

        UserEntity questioner = entityManager.getReference(UserEntity.class, questionerId);


        NotificationEntity notificationEntity = NotificationEntity.builder()
                .user(questioner)
                .course(null)
                .content(content)
                .domainId(qna.getId())
                .domain(NotificationDomain.QNA)
                .isRead(false)
                .build();

        notificationService.createNotification(notificationEntity);
    }

    private void sendMentoringStatusNotification(Long requestId, String status, String comment) {
        MentoringRequestEntity mentoringRequestEntity = mentoringService.getMentoringRequestEntityById(requestId);
        UserEntity mentor = mentoringRequestEntity.getMentor();
        CourseEntity course = mentoringRequestEntity.getCourse();
        UserEntity mentee = mentoringRequestEntity.getRequester();
        String content = "";

        if(mentoringRequestEntity.getStatus() == MentoringStatus.멘티취소){
            content =  String.format("%s 수업의 %s 멘티가 멘토링 요청을 취소했습니다. 사유: %s", course.getName(), mentee.getName(), comment);
        }
        else {
            content = String.format("%s 수업의 %s 멘토가 멘토링 요청을 %s했습니다. 사유: %s",
                    course.getName(), mentor.getName(), status, comment);
        }


        NotificationEntity notificationEntity = NotificationEntity.builder()
                .user(mentee)
                .course(course)
                .content(content)
                .domain(NotificationDomain.MENTORING)
                .domainId(mentoringRequestEntity.getId())
                .isRead(false)
                .build();

        notificationService.createNotification(notificationEntity);
    }

    private void sendNotificationAfterCourseRoleChanged(Long courseId, Long userId, Short role) {
        // 강의 및 사용자 조회
        CourseEntity course = courseService.findCourseById(courseId);
        UserEntity user = userService.findUserInfoById(userId);

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


        String content = String.format("%s님이 %s 수업의 %s 역할로 변경되었습니다.",
                user.getName(), course.getName(), roleName);




        NotificationEntity notificationEntity = NotificationEntity.builder()
                .user(user)
                .course(course)
                .content(content)
                .domainId(course.getId())
                .domain(NotificationDomain.COURSE)
                .isRead(false)
                .build();


        // 알림 생성
        notificationService.createNotification(notificationEntity);
    }


}

