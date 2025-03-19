package pheonix.classconnect.backend.aop.mail;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import pheonix.classconnect.backend.com.user.entity.UserEntity;
import pheonix.classconnect.backend.course.entity.CourseEntity;
import pheonix.classconnect.backend.mentoring.contants.MentoringStatus;
import pheonix.classconnect.backend.mentoring.entity.MentoringRequestEntity;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@Service("localMailService")
@RequiredArgsConstructor
@Slf4j
public class LocalMailService implements MailService {

    @Value("${my-app.base-url}")
    private String baseUrl;

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;



    @Override
    public void sendMentoringMail(MentoringRequestEntity mentoring) {
        UserEntity sender;
        UserEntity receiver;
        String action;
        String role;
        short status = mentoring.getStatus();

        if (status == MentoringStatus.승인대기) {
            sender = mentoring.getRequester();
            receiver = mentoring.getMentor();
            action = "신청";
            role = "멘티님";
        } else if (status == MentoringStatus.승인) {
            sender = mentoring.getMentor();
            receiver = mentoring.getRequester();
            action = "승인";
            role = "멘토님";
        } else if (status == MentoringStatus.반려) {
            sender = mentoring.getMentor();
            receiver = mentoring.getRequester();
            action = "반려";
            role = "멘토님";
        } else if (status == MentoringStatus.멘티취소) {
            sender = mentoring.getRequester();
            receiver = mentoring.getMentor();
            action = "취소";
            role = "멘티님";
        } else if (status == MentoringStatus.멘토취소) {
            sender = mentoring.getMentor();
            receiver = mentoring.getRequester();
            action = "취소";
            role = "멘토님";
        } else {
            log.warn("알 수 없는 멘토링 상태: {}", status);
            return;
        }


        try {
            String subject = String.format("ClassConnect %s 멘토링 안내", mentoring.getCourse().getName());
            Map<String, Object> variables = new HashMap<>();
            variables.put("className", mentoring.getCourse().getName());
            variables.put("courseRole", role);
            variables.put("name", sender.getName());
            variables.put("mentoringAction", action);
            variables.put("url", baseUrl + "/" + mentoring.getCourse().getId() + "/mentoring");

            Context context = new Context();
            context.setVariables(variables);

            String htmlBody = templateEngine.process("MentoringCreateMail.html", context);

            sendHtmlMessage(receiver.getEmail(), subject, htmlBody);
        } catch (MessagingException e) {
            log.error("멘토링 메일 전송 실패: {}", e.getMessage());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendHtmlMessage(String to, String subject, String htmlBody) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);

        helper.setText(htmlBody, true);

        message.setHeader("MIME-Version", "1.0");
        message.setHeader("Content-Type", "text/html; charset=UTF-8");
        message.setHeader("Content-Transfer-Encoding", "quoted-printable");

        // 이메일 전송
        mailSender.send(message);
    }





    public void sendMentoringReminderMail(MentoringRequestEntity mentoring) {
        UserEntity mentor = mentoring.getMentor();
        UserEntity mentee = mentoring.getRequester();
        CourseEntity course = mentoring.getCourse();

        try {
            String subject = String.format("[ClassConnect] %s 멘토링 진행 안내 (24시간 전)", course.getName());

            Map<String, Object> variables = new HashMap<>();
            variables.put("className", course.getName());
            variables.put("mentorName", mentor.getName());
            variables.put("menteeName", mentee.getName());
            variables.put("date", mentoring.getDate().toString());
            variables.put("startTime", mentoring.getStartTime().toString());
            variables.put("url", baseUrl + "/" + course.getId() + "/mentoring");

            Context context = new Context();
            context.setVariables(variables);

            String htmlBody = templateEngine.process("MentoringReminderMail.html", context);

            sendHtmlMessage(mentor.getEmail(), subject, htmlBody);
            sendHtmlMessage(mentee.getEmail(), subject, htmlBody);

            log.info("멘토링 리마인더 메일 전송 완료: {} - {}", mentor.getEmail(), mentee.getEmail());
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("멘토링 리마인더 메일 전송 실패: {}", e.getMessage());
        }
    }

}
