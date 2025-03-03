package pheonix.classconnect.backend.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pheonix.classconnect.backend.notification.service.NotificationService;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 15 * * ?", zone = "Asia/Seoul") // 한국시간(KST) 자정
    public void scheduleMentoringNotifications() {
        notificationService.createMentoringNotifications();
    }

    @Scheduled(cron = "0 0 15 * 10 ?", zone = "Asia/Seoul") // 한국시간(KST) 자정
    public void scheduleTimeTableNotifications() {

        notificationService.createTimeTableNotifications();
    }
}
