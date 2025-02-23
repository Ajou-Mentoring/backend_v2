package pheonix.classconnect.backend.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pheonix.classconnect.backend.com.common.model.Response;
import pheonix.classconnect.backend.notification.service.NotificationService;
import pheonix.classconnect.backend.security.utils.PrincipalUtils;

@RestController
@RequestMapping("/v2/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @PatchMapping("/{id}/read")
    public Response updateNotificationIsRead(@AuthenticationPrincipal org.springframework.security.core.userdetails.User user  ,
                                             @PathVariable(name="id") Long notificationId){
        notificationService.updateIsRead(notificationId, PrincipalUtils.getUserId(user));
        return Response.ok("알림을 읽은 상태로 변경하였습니다.");
    }

    @PatchMapping("/read")
    public Response updateAllNotificationsRead(@AuthenticationPrincipal org.springframework.security.core.userdetails.User user  ){
        notificationService.updateAllNotificationsRead(PrincipalUtils.getUserId(user));
        return Response.ok("모든 알림을 읽은 상태로 변경하였습니다.");
    }
}
