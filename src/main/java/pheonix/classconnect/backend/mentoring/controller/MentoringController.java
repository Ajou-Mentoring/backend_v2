package pheonix.classconnect.backend.mentoring.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import pheonix.classconnect.backend.com.attachment.model.response.FileResponse;
import pheonix.classconnect.backend.com.common.model.Response;
import pheonix.classconnect.backend.com.user.model.UserDTO;
import pheonix.classconnect.backend.course.constants.CourseRole;
import pheonix.classconnect.backend.course.service.CourseMemberService;
import pheonix.classconnect.backend.exceptions.ErrorCode;
import pheonix.classconnect.backend.exceptions.MainApplicationException;
import pheonix.classconnect.backend.mentoring.model.MentoringRequestDTO;
import pheonix.classconnect.backend.mentoring.model.ScheduleDTO;
import pheonix.classconnect.backend.mentoring.service.MentoringService;
import pheonix.classconnect.backend.security.service.PrincipalDetailsService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/v2")
@RequiredArgsConstructor
public class MentoringController {
    private final MentoringService mentoringService;
    private final PrincipalDetailsService principalDetailsService;
    private final CourseMemberService courseMemberService;

    // 멘토링 신청 가능 시간 조회
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

    // 멘토링 신청
    @PostMapping("/courses/{courseId}/requests")
    public Response postMentoringRequest(@PathVariable(value = "courseId") Long courseId,
                                         @RequestBody MentoringRequestDTO.Request01 req,
                                         @AuthenticationPrincipal User user) {
        log.info("MentoringController.postMentoringRequest()");

        // 입력값 검증
        if (user == null) {
            throw new MainApplicationException(ErrorCode.BACK_INVALID_PERMISSION, "권한 정보가 없습니다.");
        }

        if (req.getMentees().isEmpty()) {
            throw new MainApplicationException(ErrorCode.MENTORING_REQUEST_INVALID_PARAMETER, "멘티 목록이 비어있습니다.");
        }

        Map<String, Object> mentees = new HashMap<>();
        for (MentoringRequestDTO.Mentee mentee : req.getMentees()) {
            if (mentees.containsKey(mentee.getStudentNo())) {
                throw new MainApplicationException(ErrorCode.MENTORING_REQUEST_INVALID_PARAMETER, String.format("멘티 학번 중복입니다. [%s]", mentee.getStudentNo()));
            }
            mentees.put(mentee.getStudentNo(), mentee.getName());
        }
        MentoringRequestDTO.Create dto = MentoringRequestDTO.Create.builder()
                .mentorId(req.getMentorId())
                .date(req.getDate())
                .requesterId(Long.parseLong(user.getUsername()))
                .courseId(courseId)
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .site(req.getSite())
                .content(req.getContent())
                .mentees(mentees)
                .images(req.getImages())
                .files(req.getFiles())
                .build();

        mentoringService.createRequest(dto);

        return Response.ok(HttpStatus.CREATED, "멘토링 요청을 생성하였습니다.", null);
    }

    // 월별 멘토링 신청 내역 조회
    @GetMapping("/courses/{courseId}/requests")
    public Response<List<MentoringRequestDTO.Response01>> getMentoringRequests(@PathVariable(value = "courseId") Long courseId,
                                                                               @RequestParam(value = "year") int year,
                                                                               @RequestParam(value = "month") int month,
                                                                               @RequestParam(value = "mentor", required = false) Long mentorId,
                                                                               @RequestParam(value = "requester", required = false) Long requesterId,
                                                                               @AuthenticationPrincipal User user) {
        log.info("MentoringController.getMentoringRequests({} {} {} {} {})", courseId, year, month, mentorId, requesterId);

        // 입력값 검증
        if (user == null) {
            throw new MainApplicationException(ErrorCode.BACK_INVALID_PERMISSION, "권한 정보가 없습니다.");
        }
        // 관리자 또는 코스 멤버가 아니라면 조회 권한 없음
        if (!courseMemberService.isCourseMember(courseId, Long.parseLong(user.getUsername())) && !principalDetailsService.isAdmin(user) ) {
            throw new MainApplicationException(ErrorCode.MENTOR_UNAUTHORIZED, "코스 멤버 또는 관리자만 접근 가능한 요청입니다.");
        }

        List<MentoringRequestDTO.Response01> res = mentoringService.getMentoringRequests(mentorId, requesterId, courseId, year, month).stream()
                .map(request -> MentoringRequestDTO.Response01.builder()
                        .id(request.getId())
                        .mentor(UserDTO.Response01.builder()
                                .id(request.getMentor().getId())
                                .name(request.getMentor().getName())
                                .studentNo(request.getMentor().getStudentNo())
                                .email(request.getMentor().getEmail())
                                .courseRole(CourseRole.MENTOR)
                                .build())
                        .date(request.getDate())
                        .startTime(request.getStartTime())
                        .endTime(request.getEndTime())
                        .site(request.getSite())
                        .content(request.getContent())
                        .mentees(request.getMentees().entrySet().stream()
                                .map(mentee -> MentoringRequestDTO.Mentee.builder()
                                        .studentNo(mentee.getKey())
                                        .name((String) mentee.getValue())
                                        .build())
                                .toList())
                        .isRegistered(request.getRegistered())
                        .images(request.getImages().stream()
                                .map(FileResponse.Info::fromFile)
                                .toList())
                        .files(request.getFiles().stream()
                                .map(FileResponse.Info::fromFile)
                                .toList())
                        .build())
                .toList();

        return Response.ok(HttpStatus.OK, "멘토링 요청을 조회하였습니다.", res);
    }

    // 멘토링 상세 조회
    @GetMapping("/courses/{courseId}/requests/{requestId}")
    public Response<MentoringRequestDTO.Response01> getMentoringRequest(@PathVariable(value = "courseId") Long courseId,
                                                                              @PathVariable(value = "requestId") Long requestId,
                                                                              @AuthenticationPrincipal User user) {
        log.info("MentoringController.getMentoringRequest({} {})", courseId, requestId);

        // 입력값 검증
        if (user == null) {
            throw new MainApplicationException(ErrorCode.BACK_INVALID_PERMISSION, "권한 정보가 없습니다.");
        }
        // 관리자 또는 코스 멤버가 아니라면 조회 권한 없음
        if (!courseMemberService.isCourseMember(courseId, Long.parseLong(user.getUsername())) || !principalDetailsService.isAdmin(user) ) {
            throw new MainApplicationException(ErrorCode.MENTOR_UNAUTHORIZED, "코스 멤버 또는 관리자만 접근 가능한 요청입니다.");
        }

        MentoringRequestDTO.MentoringRequest request = mentoringService.getMentoringRequest(requestId);

        MentoringRequestDTO.Response01 res = MentoringRequestDTO.Response01.builder()
                .id(request.getId())
                .mentor(UserDTO.Response01.builder()
                        .id(request.getMentor().getId())
                        .name(request.getMentor().getName())
                        .studentNo(request.getMentor().getStudentNo())
                        .email(request.getMentor().getEmail())
                        .courseRole(CourseRole.MENTOR)
                        .build())
                .date(request.getDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .site(request.getSite())
                .mentees(request.getMentees().entrySet().stream()
                        .map(mentee -> MentoringRequestDTO.Mentee.builder()
                                .studentNo(mentee.getKey())
                                .name((String) mentee.getValue())
                                .build())
                        .toList())
                .isRegistered(request.getRegistered())
                .images(request.getImages().stream()
                        .map(FileResponse.Info::fromFile)
                        .toList())
                .files(request.getFiles().stream()
                        .map(FileResponse.Info::fromFile)
                        .toList())
                .build();

        return Response.ok(HttpStatus.OK, "멘토링 요청을 조회했습니다.", res);
    }

    @PutMapping("/courses/{courseId}/requests/{requestId}")
    public Response updateMentoringRequest(@PathVariable(value = "courseId") Long courseId,
                                                                            @PathVariable(value = "requestId") Long requestId,
                                                                            @RequestBody MentoringRequestDTO.Request02 req,
                                                                            @AuthenticationPrincipal User user) {
        log.info("MentoringController.processMentoringRequest({} {})", courseId, requestId);

        // 입력값 검증
        if (user == null) {
            throw new MainApplicationException(ErrorCode.BACK_INVALID_PERMISSION, "권한 정보가 없습니다.");
        }

        MentoringRequestDTO.MentoringRequest request = mentoringService.getMentoringRequest(requestId);

        // 멘토링 신청자가 아니라면 수정 권한 없음
        if (request.getRequester().getId() != Long.parseLong(user.getUsername())) {
            throw new MainApplicationException(ErrorCode.MENTOR_UNAUTHORIZED, "멘토링 요청 내역은 신청자만 수정 가능합니다.");
        }

        if (request.getMentees().isEmpty()) {
            throw new MainApplicationException(ErrorCode.MENTORING_REQUEST_INVALID_PARAMETER, "멘티 목록이 비어있습니다.");
        }

        Map<String, Object> mentees = new HashMap<>();
        for (MentoringRequestDTO.Mentee mentee : req.getMentees()) {
            if (mentees.containsKey(mentee.getStudentNo())) {
                throw new MainApplicationException(ErrorCode.MENTORING_REQUEST_INVALID_PARAMETER, String.format("멘티 학번 중복입니다. [%s]", mentee.getStudentNo()));
            }
            mentees.put(mentee.getStudentNo(), mentee.getName());
        }

        MentoringRequestDTO.Update dto = MentoringRequestDTO.Update.builder()
                .mentees(mentees)
                .content(req.getContent())
                .site(req.getSite())
                .images(req.getImages())
                .files(req.getFiles())
                .build();

        // 본처리
        mentoringService.updateRequest(requestId, dto);

        return Response.ok(HttpStatus.ACCEPTED, "멘토링 요청이 수정되었습니다.", null);
    }
}
