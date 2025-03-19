package pheonix.classconnect.backend.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pheonix.classconnect.backend.aop.mail.LocalMailService;
import pheonix.classconnect.backend.mentoring.contants.MentoringStatus;
import pheonix.classconnect.backend.mentoring.entity.MentoringRequestEntity;
import pheonix.classconnect.backend.mentoring.repository.MentoringRequestRepository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;


@Component
@RequiredArgsConstructor
@Slf4j
public class MailScheduler {

    private final MentoringRequestRepository mentoringRequestRepository;
    private final LocalMailService mailService;

    @Scheduled(cron = "0 0 8 * * ?", zone = "Asia/Seoul") // 매일 오전 8시 실행 (KST)
    public void sendMentoringReminderMails() {
        log.info("멘토링 리마인더 메일 전송 시작...");

        // 현재 한국 시간 기준으로 하루 뒤 날짜 계산
        LocalDate reminderDate = LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(1);

        // 멘토링 상태가 '승인'이고, 하루 뒤에 진행될 멘토링 요청 조회
        List<MentoringRequestEntity> upcomingMentorings = mentoringRequestRepository.findByStatusAndDate(
                MentoringStatus.승인, reminderDate
        );

        if (upcomingMentorings.isEmpty()) {
            log.info("리마인더 대상 멘토링 없음.");
            return;
        }

        for (MentoringRequestEntity mentoring : upcomingMentorings) {
            try {
                mailService.sendMentoringReminderMail(mentoring);
                log.info("리마인더 메일 전송 완료 - 멘토링 ID: {}", mentoring.getId());
            } catch (Exception e) {
                log.error("멘토링 리마인더 메일 전송 실패 - ID: {}, 에러: {}", mentoring.getId(), e.getMessage());
            }
        }
    }
}
