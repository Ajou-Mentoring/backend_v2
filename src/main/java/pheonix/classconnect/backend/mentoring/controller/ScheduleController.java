package pheonix.classconnect.backend.mentoring.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import pheonix.classconnect.backend.com.common.model.Response;
import pheonix.classconnect.backend.exceptions.ErrorCode;
import pheonix.classconnect.backend.exceptions.MainApplicationException;
import pheonix.classconnect.backend.mentoring.model.ScheduleDTO;
import pheonix.classconnect.backend.mentoring.model.TimeTableDTO;
import pheonix.classconnect.backend.mentoring.service.ScheduleService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/v2")
@RequiredArgsConstructor
public class ScheduleController {
    private final ScheduleService scheduleService;

    @PutMapping("/users/{userId}/schedule/weekly")
    public Response<List<String>> checkWeeklySchedule(@PathVariable(value = "userId") Long userId,
                                                 @RequestBody TimeTableDTO.Request01 req,
                                                 @AuthenticationPrincipal User user) {
        log.info("ScheduleController.checkWeeklySchedule({})", userId);

        // 요청 검증
        if (user == null || Long.parseLong(user.getUsername()) != userId) {
            throw new MainApplicationException(ErrorCode.MENTOR_UNAUTHORIZED, "스케줄 등록 권한이 없습니다.");
        }

        // 본처리 - 충돌되는 일정 확인
        TimeTableDTO.Create dto = TimeTableDTO.Create.builder()
                        .userId(userId)
                        .startDate(req.getStartDate())
                        .endDate(req.getEndDate())
                        .schedules(req.getTimeSlots().stream()
                            .map(slot -> ScheduleDTO.Weekly.builder()
                                    .dayOfWeek(slot.getDayOfWeek())
                                    .startTime(slot.getStartTime())
                                    .endTime(slot.getEndTime())
                                    .site(slot.getMentoringType())
                                    .build())
                            .toList())
                        .build();

        List<ScheduleDTO.Schedule> duplicated = scheduleService.getDuplicatedSchedule(dto);

        if (!duplicated.isEmpty()) {
            List<String> dateList = duplicated.stream()
                    .map(ScheduleDTO.Schedule::getDate)
                    .collect(Collectors.toSet())
                    .stream().toList()
                    .stream().sorted()
                    .map(LocalDate::toString).toList();

            return new Response<>(HttpStatus.CONFLICT, false, "충돌하는 일정이 있습니다.", dateList);
        }

        // 타임테이블은 저장
        scheduleService.saveTimeTable(dto);

        return Response.ok(HttpStatus.OK, "일정 등록이 가능한 상태입니다.", null);
    }


    @PostMapping("/users/{userId}/schedule/weekly")
    public Response<String> postWeeklySchedule(@PathVariable(value = "userId") Long userId,
                                                @RequestBody TimeTableDTO.Request01 req,
                                                @AuthenticationPrincipal User user) {
        log.info("ScheduleController.updateWeeklySchedule({})", userId);

        // 요청 검증
        if (user == null || Long.parseLong(user.getUsername()) != userId) {
            throw new MainApplicationException(ErrorCode.MENTOR_UNAUTHORIZED, "스케줄 등록 권한이 없습니다.");
        }

        // 본처리 - 충돌되는 일정 확인
        TimeTableDTO.Create dto = TimeTableDTO.Create.builder()
                .userId(userId)
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .schedules(req.getTimeSlots().stream()
                        .map(slot -> ScheduleDTO.Weekly.builder()
                                .dayOfWeek(slot.getDayOfWeek())
                                .startTime(slot.getStartTime())
                                .endTime(slot.getEndTime())
                                .site(slot.getMentoringType())
                                .build())
                        .toList())
                .build();

        // 스케줄 등록
        scheduleService.uploadWeeklySchedule(dto);

        // 타임테이블 등록
        scheduleService.saveTimeTable(dto);

        return Response.ok(HttpStatus.CREATED, "일정을 등록했습니다.", null);
    }

    @GetMapping("/users/{userId}/schedule/weekly")
    public Response<List<TimeTableDTO.Response21>> getWeeklySchedule(@PathVariable(value = "userId") Long userId) {
        log.info("ScheduleController.getWeeklySchedule({})", userId);

        List<TimeTableDTO.Response21> res = scheduleService.getTimeTable(userId).stream()
                .map(schedule ->
                    TimeTableDTO.Response21.builder()
                            .dayOfWeek(schedule.getDay().intValue())
                            .startTime(schedule.getStartTime())
                            .endTime(schedule.getEndTime())
                            .mentoringType(schedule.getSite())
                            .build()
                ).toList();

        return Response.ok(HttpStatus.OK, "주간 스케줄을 조회했습니다.", res);
    }

    @GetMapping("/users/{userId}/schedule/{year}/{month}")
    public Response<List<ScheduleDTO.Response31>> getMonthlySchedule(@PathVariable(value = "userId") Long userId,
                                                                      @PathVariable(value = "year") Integer year,
                                                                      @PathVariable(value = "month") Integer month) {
        log.info("ScheduleController.getMonthlySchedule({})", userId);

        // 요청 검증
        if (userId == null)
            throw new MainApplicationException(ErrorCode.BACK_NONNULL_PARAMETER, "파라미터가 NULL입니다. [userId]");
        if (year == null)
            throw new MainApplicationException(ErrorCode.BACK_NONNULL_PARAMETER, "파라미터가 NULL입니다. [year]");
        if (month == null)
            throw new MainApplicationException(ErrorCode.BACK_NONNULL_PARAMETER, "파라미터가 NULL입니다. [month]");

        List<ScheduleDTO.Response31> res = scheduleService.getMonthlySchedule(userId, year, month).stream()
                .map(schedule -> ScheduleDTO.Response31.builder()
                        .date(schedule.getDate())
                        .startTime(schedule.getStartTime())
                        .endTime(schedule.getEndTime())
                        .mentoringType(schedule.getSite())
                        .build())
                .toList();

        return Response.ok(HttpStatus.OK, "월간 스케줄을 조회했습니다.", res);
    }

    // 개별 일정 조회
    @GetMapping("/users/{userId}/schedule/{year}/{month}/{day}")
    public Response<List<ScheduleDTO.Response31>> getDailySchedule(@PathVariable(value = "userId") Long userId,
                                                                   @PathVariable(value = "year") Integer year,
                                                                   @PathVariable(value = "month") Integer month,
                                                                   @PathVariable(value = "day") Integer day
    ) {
        log.info("ScheduleController.getDailySchedule({}, {}, {}, {})", userId, year, month, day);

        // 요청 검증
        if (userId == null)
            throw new MainApplicationException(ErrorCode.BACK_NONNULL_PARAMETER, "파라미터가 NULL입니다. [userId]");
        if (year == null)
            throw new MainApplicationException(ErrorCode.BACK_NONNULL_PARAMETER, "파라미터가 NULL입니다. [year]");
        if (month == null)
            throw new MainApplicationException(ErrorCode.BACK_NONNULL_PARAMETER, "파라미터가 NULL입니다. [month]");
        if (day == null)
            throw new MainApplicationException(ErrorCode.BACK_NONNULL_PARAMETER, "파라미터가 NULL입니다. [day]");

        List<ScheduleDTO.Response31> res = scheduleService.getDailySchedule(userId, year, month, day).stream()
                .map(schedule -> ScheduleDTO.Response31.builder()
                        .date(schedule.getDate())
                        .startTime(schedule.getStartTime())
                        .endTime(schedule.getEndTime())
                        .mentoringType(schedule.getSite())
                        .build())
                .toList();

        return Response.ok(HttpStatus.OK, "일일 스케줄을 조회했습니다.", res);
    }

    // 일일 일정 수정
    @PostMapping("/users/{userId}/schedule/{year}/{month}/{day}")
    public Response<String> postDailySchedule(@PathVariable(value = "userId") Long userId,
                                              @PathVariable(value = "year") Integer year,
                                              @PathVariable(value = "month") Integer month,
                                              @PathVariable(value = "day") Integer day,
                                              @RequestBody List<ScheduleDTO.Request02> req,
                                              @AuthenticationPrincipal User user
    ) {
        log.info("ScheduleController.postDailySchedule({}, {}, {}, {})", userId, year, month, day);

        // 요청 검증
        if (userId == null)
            throw new MainApplicationException(ErrorCode.BACK_NONNULL_PARAMETER, "파라미터가 NULL입니다. [userId]");
        if (year == null)
            throw new MainApplicationException(ErrorCode.BACK_NONNULL_PARAMETER, "파라미터가 NULL입니다. [year]");
        if (month == null)
            throw new MainApplicationException(ErrorCode.BACK_NONNULL_PARAMETER, "파라미터가 NULL입니다. [month]");
        if (day == null)
            throw new MainApplicationException(ErrorCode.BACK_NONNULL_PARAMETER, "파라미터가 NULL입니다. [day]");

        if (user == null || Long.parseLong(user.getUsername()) != userId) {
            throw new MainApplicationException(ErrorCode.BACK_INVALID_PERMISSION, "일정 등록/수정 권한이 없습니다.");
        }

        List<ScheduleDTO.Create> dto = req.stream()
                .map(schedule -> ScheduleDTO.Create.builder()
                        .startTime(schedule.getStartTime())
                        .endTime(schedule.getEndTime())
                        .mentoringType(schedule.getMentoringType())
                        .build()
                ).toList();

        scheduleService.saveDailySchedule(userId, LocalDate.of(year, month, day), dto);

        return Response.ok(HttpStatus.CREATED, "일일 스케줄을 등록했습니다.", null);
    }
}
