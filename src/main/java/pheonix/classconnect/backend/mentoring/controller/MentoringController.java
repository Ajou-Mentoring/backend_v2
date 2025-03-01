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
import pheonix.classconnect.backend.course.model.CourseDTO;
import pheonix.classconnect.backend.course.service.CourseMemberService;
import pheonix.classconnect.backend.exceptions.ErrorCode;
import pheonix.classconnect.backend.exceptions.MainApplicationException;
import pheonix.classconnect.backend.mentoring.model.MentoringRequestDTO;
import pheonix.classconnect.backend.mentoring.model.MentoringResultDTO;
import pheonix.classconnect.backend.mentoring.model.ScheduleDTO;
import pheonix.classconnect.backend.mentoring.service.MentoringService;
import pheonix.classconnect.backend.security.service.PrincipalDetailsService;

import java.time.LocalDate;
import java.util.*;

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
                        .mentoringType(s.getSite())
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
                .site(req.getMentoringType())
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
                                                                               @RequestParam(value = "mentee", required = false) Long menteeId,
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

        List<MentoringRequestDTO.Response01> res = mentoringService.getMentoringRequests(mentorId, requesterId, menteeId, courseId, year, month).stream()
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
                        .mentoringType(request.getSite())
                        .content(request.getContent())
                        .status(request.getStatus())
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
                .mentoringType(request.getSite())
                .mentees(request.getMentees().entrySet().stream()
                        .map(mentee -> MentoringRequestDTO.Mentee.builder()
                                .studentNo(mentee.getKey())
                                .name((String) mentee.getValue())
                                .build())
                        .toList())
                .isRegistered(request.getRegistered())
                .status(request.getStatus())
                .images(request.getImages().stream()
                        .map(FileResponse.Info::fromFile)
                        .toList())
                .files(request.getFiles().stream()
                        .map(FileResponse.Info::fromFile)
                        .toList())
                .build();

        return Response.ok(HttpStatus.OK, "멘토링 요청을 조회했습니다.", res);
    }

    // 멘토링 수정
    @PutMapping("/courses/{courseId}/requests/{requestId}")
    public Response updateMentoringRequest(@PathVariable(value = "courseId") Long courseId,
                                                                            @PathVariable(value = "requestId") Long requestId,
                                                                            @RequestBody MentoringRequestDTO.Request02 req,
                                                                            @AuthenticationPrincipal User user) {
        log.info("MentoringController.updateMentoringRequest({} {})", courseId, requestId);

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
                .site(req.getMentoringType())
                .images(req.getImages())
                .files(req.getFiles())
                .build();

        // 본처리
        mentoringService.updateRequest(requestId, dto);

        return Response.ok(HttpStatus.ACCEPTED, "멘토링 요청이 수정되었습니다.", null);
    }

    @PatchMapping("/courses/{courseId}/requests/{requestId}")
    public Response processRequest(@PathVariable(value = "courseId") Long courseId,
                                   @PathVariable(value = "requestId") Long requestId,
                                   @RequestBody MentoringRequestDTO.Request03 req,
                                   @AuthenticationPrincipal User user) {
        
        log.info("MentoringController.processRequest({} {})", courseId, requestId);
        
        // 입력 검증
        if (user == null) {
            throw new MainApplicationException(ErrorCode.BACK_INVALID_PERMISSION, "권한 정보가 없습니다.");
        }
        
        // action : 1-승인 2-반려 3-취소
        if (req.getAction() == 1) {
            // 멘토링 승인은 멘토만 가능
            MentoringRequestDTO.MentoringRequest request = mentoringService.getMentoringRequest(requestId);
            if (Long.parseLong(user.getUsername()) != request.getMentor().getId()) {
                throw new MainApplicationException(ErrorCode.MENTORING_REQUEST_FORBIDDEN_REQUEST, "승인/반려 권한이 없습니다.");
            }
            mentoringService.acceptRequest(requestId, req.getComment());

            return Response.ok(HttpStatus.ACCEPTED, "멘토링 신청을 승인하였습니다.", null);
        }
        else if (req.getAction() == 2) {
            // 멘토링 반려는 멘토만 가능
            MentoringRequestDTO.MentoringRequest request = mentoringService.getMentoringRequest(requestId);
            if (Long.parseLong(user.getUsername()) != request.getMentor().getId()) {
                throw new MainApplicationException(ErrorCode.MENTORING_REQUEST_FORBIDDEN_REQUEST, "승인/반려 권한이 없습니다.");
            }
            mentoringService.rejectRequest(requestId, req.getComment());

            return Response.ok(HttpStatus.ACCEPTED, "멘토링 신청을 반려하였습니다.", null);
        }
        else if (req.getAction() == 3) {
            // 멘토링 취소는 신청자만 가능
            MentoringRequestDTO.MentoringRequest request = mentoringService.getMentoringRequest(requestId);
            Long usrId = Long.parseLong(user.getUsername());
            if (!(usrId.equals(request.getRequester().getId()) || usrId.equals(request.getMentor().getId()))) {
                throw new MainApplicationException(ErrorCode.MENTORING_REQUEST_FORBIDDEN_REQUEST, "취소 권한이 없습니다.");
            }
            mentoringService.cancelRequest(requestId, courseId, req.getComment(), usrId);

            return Response.ok(HttpStatus.ACCEPTED, "멘토링 신청을 취소하였습니다.", null);
        }

        throw new MainApplicationException(ErrorCode.MENTORING_REQUEST_INVALID_PARAMETER, String.format("지원하지 않는 처리 구분입니다. [%d]", req.getAction()));


    }

    @GetMapping("/courses/{courseId}/members/{memberId}/stats")
    public Response<List<UserDTO.Response04>> getMembersStats(@PathVariable(value = "courseId") Long courseId,
                                                              @PathVariable(value = "memberId") Long memberId,
                                                              @RequestParam(value = "year", defaultValue = "0") int year,
                                                              @RequestParam(value = "month", defaultValue = "0") int month,
                                                              @RequestParam(value = "groupBy") String groupBy,
                                                              @AuthenticationPrincipal User user)
    {
        log.info("CourseController.getMemberStats({}, {}, {}, {}, {})", courseId, memberId, year, month, groupBy);

        // 요청 검증
        if (user == null) {
            throw new MainApplicationException(ErrorCode.BACK_INVALID_PERMISSION, "사용자 권한 정보가 없습니다.");
        }
        else if (!principalDetailsService.isAdmin(user)) {
            throw new MainApplicationException(ErrorCode.BACK_INVALID_PERMISSION, "통계 자료는 관리자만 접근 가능합니다.");
        }
        if (courseId == null) {
            throw new MainApplicationException(ErrorCode.COURSE_INVALID_PARAMETER, "코스 ID는 필수 값입니다.");
        }
        if (memberId == null) {
            throw new MainApplicationException(ErrorCode.COURSE_INVALID_PARAMETER, "멤버 ID는 필수 값입니다.");
        }
        if (year == 0) {
            throw new MainApplicationException(ErrorCode.COURSE_INVALID_PARAMETER, "연도는 필수 값입니다.");
        }
        if (month == 0) {
            throw new MainApplicationException(ErrorCode.COURSE_INVALID_PARAMETER, "월은 필수 값입니다.");
        }
        if (groupBy == null) {
            throw new MainApplicationException(ErrorCode.COURSE_INVALID_PARAMETER, "통계 기준(groupBy)은 필수 값입니다.");
        }

        List<UserDTO.Response04> res = new ArrayList<>();

        // 1. 멘토링 신청 현황(통계) 조회
        if (groupBy.equalsIgnoreCase("REQUEST")) {
            // memberId == 0 일 경우 전체 조회
            if (memberId == 0L) {
                // 멘토링 현황 조회 (승인 건만)
                List<CourseDTO.Member> members = courseMemberService.findMembersInCourse(courseId);

                for (CourseDTO.Member member : members) {
                    // 멘티가 아니라면 skip
                    if (!Objects.equals(member.getCourseRole(), CourseRole.MENTEE))
                        continue;
                    int count = 0;
                    try {
                        count = mentoringService.getMentoringRequestCount(null, member.getUser().getId(), courseId, year, month);
                    } catch (MainApplicationException e) {
                        // 서비스에 가입한 멘티가 아닌 경우 출력에서 제외
                        if (e.getErrorCode().equals(ErrorCode.MENTEE_NOT_FOUND))
                            continue;
                    }
                    // 멘토링 횟수가 0이라면 skip
                    if (count == 0)
                        continue;

                    res.add(UserDTO.Response04.builder()
                            .id(member.getUser().getId())
                            .email(member.getUser().getEmail())
                            .name(member.getUser().getName())
                            .studentNo(member.getUser().getStudentNo())
                            .courseRole(member.getCourseRole())
                            .count(count)
                            .build());
                }
            }
        }
        // 2. 증빙자료 현황(통계) 조회
        if (groupBy.equalsIgnoreCase("RESULT")) {
            // memberId == 0 일 경우 전체 조회
            if (memberId == 0L) {
                // 멘토링 현황 조회 (승인 건만)
                List<UserDTO.User> mentors = courseMemberService.findMentors(courseId);

                for (UserDTO.User mentor : mentors) {
                    int count = mentoringService.getMentoringResultCount(mentor.getId(), courseId, year, month);

                    res.add(UserDTO.Response04.builder()
                            .id(mentor.getId())
                            .email(mentor.getEmail())
                            .name(mentor.getName())
                            .studentNo(mentor.getStudentNo())
                            .courseRole(CourseRole.MENTOR)
                            .count(count)
                            .build());
                }
            }
        }


        return Response.ok(HttpStatus.OK, String.format("%d년 %d월 멘토링 진행 현황을 조회했습니다.", year, month), res);
    }

    // 증빙자료 조회
    @GetMapping("/courses/{courseId}/results")
    public Response<List<MentoringResultDTO.Response01>> getMentoringResults(@PathVariable(value = "courseId") Long courseId,
                                                                             @RequestParam(value = "mentorId") Long mentorId,
                                                                             @RequestParam(value = "year", defaultValue = "2025") int year,
                                                                             @RequestParam(value = "month", defaultValue = "3") int month,
                                                                             @AuthenticationPrincipal User user) {
        log.info("MentoringController.getMentoringResults({}, {}, {}, {})", courseId, mentorId, year, month);

        // 요청 검증
        if (courseId == null)
            throw new MainApplicationException(ErrorCode.MENTORING_RESULT_PARAMETER_NULL, "코스 ID는 필수 값입니다.");
        if (mentorId == null)
            throw new MainApplicationException(ErrorCode.MENTORING_RESULT_PARAMETER_NULL, "코스 ID는 필수 값입니다.");

        if (user == null || (!principalDetailsService.isAdmin(user)) && (Long.parseLong(user.getUsername()) != mentorId)) {
            throw new MainApplicationException(ErrorCode.BACK_INVALID_PERMISSION, "요청 권한이 없습니다.");
        }

        List<MentoringResultDTO.Response01> respond = mentoringService.getMentoringResults(courseId, mentorId, year, month).stream()
                .map(res -> MentoringResultDTO.Response01.builder()
                        .id(res.getId())
                        .date(res.getDate())
                        .time(res.getTime())
                        .content(res.getContent())
                        .duration(res.getLength())
                        .location(res.getLocation())
                        .images(res.getImages().stream()
                                .map(FileResponse.Info::fromFile)
                                .toList())
                        .mentees(res.getMentees().keySet().stream()
                                .map(key -> MentoringResultDTO.Mentee.builder()
                                        .name(res.getMentees().get(key).toString())
                                        .studentNo(key)
                                        .build())
                                .toList())
                        .action(0)
                        .build())
                .toList();


        return Response.ok(HttpStatus.OK, "증빙자료 리스트를 조회했습니다.", respond);
    }

    // 증빙자료 업로드 (단일)
    @PostMapping("/courses/{courseId}/results")
    public Response postMentoringResult(@PathVariable(value = "courseId") Long courseId,
                                        @RequestBody MentoringResultDTO.Request01 req,
                                        @AuthenticationPrincipal User user) {
        log.info("MentoringController.postMentoringResult({})", courseId);

        // 요청 검증
        if (courseId == null) {
            throw new MainApplicationException(ErrorCode.MENTORING_RESULT_PARAMETER_NULL, "코스 ID가 널입니다.");
        }
        if (user == null) {
            throw new MainApplicationException(ErrorCode.BACK_INVALID_PERMISSION, "권한 정보가 없습니다.");
        }

        if (req.getMentees().isEmpty()) {
            throw new MainApplicationException(ErrorCode.MENTORING_RESULT_INVALID_PARAMETER, "멘티 목록이 비어있습니다.");
        }

        MentoringResultDTO.Create dto = MentoringResultDTO.Create.builder()
                .date(req.getDate())
                .time(req.getTime())
                .length(req.getDuration())
                .content(req.getContent())
                .location(req.getLocation())
                .mentees(getMenteesFromList(req.getMentees()))
                .images(req.getImages())
                .mentorId(Long.parseLong(user.getUsername()))
                .courseId(courseId)
                .requestId(req.getRequest())
                .build();

        mentoringService.createLog(dto);

        return Response.ok(HttpStatus.CREATED, "증빙자료를 생성하였습니다.", null);
    }

    private static Map<String, Object> getMenteesFromList(List<MentoringResultDTO.Mentee> menteeList) {
        Map<String, Object> mentees = new HashMap<>();
        for (MentoringResultDTO.Mentee mentee : menteeList) {
            if (mentees.containsKey(mentee.getStudentNo())) {
                throw new MainApplicationException(ErrorCode.MENTORING_RESULT_INVALID_PARAMETER, String.format("멘티 학번 중복입니다. [%s]", mentee.getStudentNo()));
            }
            mentees.put(mentee.getStudentNo(), mentee.getName());
        }
        return mentees;
    }

    // 증빙자료 수정
    @PatchMapping("courses/{courseId}/results")
    public Response patchMentoringResults(@PathVariable(value = "courseId") Long courseId,
                                          @AuthenticationPrincipal User user,
                                          @RequestBody List<MentoringResultDTO.Request02> req)
    {
        log.info("MentoringController().patchMentoringResults({})", courseId);

        // 요청 검증
        if (courseId == null) {
            throw new MainApplicationException(ErrorCode.MENTORING_RESULT_PARAMETER_NULL, "코스 ID가 널입니다.");
        }
        if (user == null) {
            throw new MainApplicationException(ErrorCode.BACK_INVALID_PERMISSION, "권한 정보가 없습니다.");
        }

        List<MentoringResultDTO.Update> dtoList = req.stream()
                .map(result -> MentoringResultDTO.Update.builder()
                        .id(result.getId())
                        .date(result.getDate())
                        .time(result.getTime())
                        .length(result.getDuration())
                        .location(result.getLocation())
                        .content(result.getContent())
                        .images(result.getImages())
                        .mentees(getMenteesFromList(result.getMentees()))
                        .action(result.getAction())
                        .build())
                        .toList();

        mentoringService.patchMentoringLogs(courseId, Long.parseLong(user.getUsername()), dtoList);

        return Response.ok(HttpStatus.ACCEPTED, "증빙자료를 수정했습니다.", null);
    }
}
