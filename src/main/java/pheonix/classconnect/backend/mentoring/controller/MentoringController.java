package pheonix.classconnect.backend.mentoring.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pheonix.classconnect.backend.com.common.model.Response;
import pheonix.classconnect.backend.mentoring.model.ScheduleDTO;
import pheonix.classconnect.backend.mentoring.service.MentoringService;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v2")
@RequiredArgsConstructor
public class MentoringController {
    private final MentoringService mentoringService;

    @GetMapping("/courses/{courseId}/mentors/{mentorId}/schedule/{year}/{month}/{day}")
    public Response<List<ScheduleDTO.Response01>> getAvailableTime(@PathVariable("courseId") Long courseId,
                                             @PathVariable("mentorId") Long mentorId,
                                             @PathVariable("year") Integer year,
                                             @PathVariable("month") Integer month,
                                             @PathVariable("day") Integer day) {
        log.info("MentoringController.getAvailableTime({}, {}, {}, {}, {})", courseId, mentorId, year, month, day);

        List<ScheduleDTO.Response01> res = mentoringService.getAvailableSchedule(mentorId, LocalDate.of(year, month, day)).stream()
                .map(s -> ScheduleDTO.Response01.builder()
                        .startTime(s.getStartTime())
                        .endTime(s.getEndTime())
                        .site(s.getSite())
                        .build())
                .toList();


        return Response.ok(HttpStatus.OK, "멘토링 일정을 조회했습니다.", res);
    }
}
