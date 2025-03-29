package pheonix.classconnect.backend.aop.mail;

import pheonix.classconnect.backend.mentoring.entity.MentoringRequestEntity;


public interface MailService {
//    void sendCourseInvitationMail(Course course, CourseRole role, String receiver);
    void sendMentoringMail(MentoringRequestEntity mentoring);
}
