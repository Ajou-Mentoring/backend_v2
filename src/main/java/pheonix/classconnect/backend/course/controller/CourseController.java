package pheonix.classconnect.backend.course.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pheonix.classconnect.backend.com.attachment.constants.AttachmentDomainType;
import pheonix.classconnect.backend.com.attachment.model.File;
import pheonix.classconnect.backend.com.attachment.model.response.FileResponse;
import pheonix.classconnect.backend.com.attachment.service.FileStorage;
import pheonix.classconnect.backend.com.common.model.Response;
import pheonix.classconnect.backend.com.user.model.UserDTO;
import pheonix.classconnect.backend.course.constants.CourseRole;
import pheonix.classconnect.backend.course.entity.CourseMemberEntity;
import pheonix.classconnect.backend.course.model.CourseDTO;
import pheonix.classconnect.backend.course.model.request.CourseCreateRequestDTO;
import pheonix.classconnect.backend.course.model.request.CourseFetchRequestDTO;
import pheonix.classconnect.backend.course.model.response.CourseResponse;
import pheonix.classconnect.backend.course.model.response.SemesterDetailsResponse;
import pheonix.classconnect.backend.course.service.CourseService;
import pheonix.classconnect.backend.course.service.CourseMemberService;
import pheonix.classconnect.backend.exceptions.ErrorCode;
import pheonix.classconnect.backend.exceptions.MainApplicationException;
import pheonix.classconnect.backend.security.service.PrincipalDetailsService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/v2")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final CourseMemberService courseMemberService;
    private final FileStorage fileStorage;
    private final PrincipalDetailsService principalDetailsService;
    private final Short domainType = AttachmentDomainType.COURSE;

    /**
     * Course 생성 API
     * @param request : Course 생성에 필요한 필드를 담은 객체
     * @param image : Course 대표 이미지
     * @param user : Course 생성하는 Principal 객체
     * @return Response
     */
    @PostMapping(value = "/courses", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE})
    public Response<String> create(@RequestPart(value = "course") @Valid CourseCreateRequestDTO request,
                           @RequestPart(value = "image", required = false) MultipartFile image,
                           @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
                           ) {

        // 요청 검증 - 관리자인지
        if (principalDetailsService.isAdmin(user)) {
            throw new MainApplicationException(ErrorCode.BAK_INVALID_PERMISSION, "관리자 권한이 없습니다.");
        }

        // 입력값 조립
        CourseDTO.Create newCourse = CourseDTO.Create.builder()
                .year(request.getYear())
                .semester(request.getSemester())
                .name(request.getName())
                .code(request.getCourseCode())
                .professorName(request.getProfessorName())
                .build();

        courseService.create(newCourse, image);

        return Response.ok("수업을 등록하였습니다.");
    }

    /**
     * Course 조회 API (학생) -> student의 ID로 참여한 Course 리스트 조회
     * @param  request : Course 리스트 필터링을 위한 파라미터를 담은 객체 (year, semester)
     * @return Response<List<CourseResponse>>
     */
    @GetMapping("/courses")
    public Response<List<CourseResponse>> getCoursesByYearAndSemester(@Valid @ModelAttribute CourseFetchRequestDTO request,
                                                                      @AuthenticationPrincipal User user){
        log.info("CourseController.getCoursesByYearAndSemester()");
        // 요청 검증
        // 관리자가 아닐 경우 자신이 참여한 코스들만 조회할 수 있다.
        if (!principalDetailsService.isAdmin(user)) {
            request.setMemberId(Long.parseLong(user.getUsername()));
        }

        // 요청 생성
        CourseDTO.Find01 findDto = CourseDTO.Find01.builder()
                .year(request.getYear())
                .semester(request.getSemester())
                .memberId(request.getMemberId())
                .build();

        List<CourseResponse> courses = courseService.getCourses(findDto).stream()
                .map(CourseResponse::fromCourse)
                .toList();

        if (!courses.isEmpty()) {
            courses.forEach(course -> {
                course.setRole(courseMemberService.findMemberRoleInClass(Long.parseLong(user.getUsername()), course.getId()));
            });
        }

        for (CourseResponse courseResponse : courses) {
            List<File> images = fileStorage.getAttachmentList(AttachmentDomainType.COURSE, courseResponse.getId());
            if (!images.isEmpty()) {
                courseResponse.setImage(FileResponse.Info.fromFile(images.getFirst()));
            }
        }

        return Response.ok(HttpStatus.OK, "수업 목록을 조회하였습니다.", courses);
    }

    /**
     * Course 삭제 API
     * @param courseId : 삭제할 Course의 ID
     * @param user : Course를 삭제하는 사용자의 Principal 객체
     * @return
     */
    @DeleteMapping("/courses/{courseId}")
    public Response delete(@PathVariable Long courseId,
                           @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {

        // 요청 검증 - 관리자인지
        if (principalDetailsService.isAdmin(user)) {
            throw new MainApplicationException(ErrorCode.BAK_INVALID_PERMISSION, "관리자 권한이 없습니다.");
        }

        courseService.delete(courseId);

        return Response.ok("수업을 삭제하였습니다.");
    }

    /**
     * 사용자가 코스를 조회할 수 있는 연도-학기 정보를 제공
     * @return
     */
    @GetMapping("/semesters")
    public Response<List<SemesterDetailsResponse>> getSemestersByStudentId(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user){

        Long studentId;
        try {
            studentId = Long.parseLong(user.getUsername());
        } catch (NumberFormatException e) {
            throw new MainApplicationException(ErrorCode.BAK_INVALID_PERMISSION, "사용자 아이디 정보가 없습니다.");
        }

//        Integer userId = PrincipalUtils.getUserId(user);
//        if(Integer.parseInt(user.getUsername()) != studentId){
//            throw new MainApplicationException(ErrorCode.INVALID_PERMISSION, "잘못된 접근입니다.");
//        }

        List<SemesterDetailsResponse> semesters = courseService.getSemestersByStudentId(studentId).stream().map(SemesterDetailsResponse::fromCourse).collect(Collectors.toList());
        return Response.ok(HttpStatus.OK, "학기를 조회하였습니다.", semesters);
    }

    /**
     * Course 정보 조회 API (일반 학생 사용자)
     * @param courseId : Course 아이디
     * @param user : 요청 사용자 정보를 담은 인증 객체
     * @return
     */
    @GetMapping("/courses/{courseId}")
    public Response<CourseResponse> getOne(@PathVariable(value = "courseId") Long courseId,
                                                         @AuthenticationPrincipal org.springframework.security.core.userdetails.User user){

        // 코스 정보 세팅
        CourseResponse response = CourseResponse.fromCourse(courseService.getACourseById(courseId));
        // 코스 이미지 정보 세팅
        response.setImage(FileResponse.Info.fromFile(fileStorage.getAttachmentList(AttachmentDomainType.COURSE, courseId).getFirst()));
        // 코스 역할 세팅
        response.setRole(courseMemberService.findMemberRoleInClass(Long.parseLong(user.getUsername()), courseId));


        return Response.ok(HttpStatus.OK, "수업 정보를 조회하였습니다.", response);
    }

    /**
     * Course 정보 조회 API (일반 학생 사용자)
     * @param courseId : Course 아이디
     * @param user : 요청 사용자 정보를 담은 인증 객체
     * @return
     */
    @GetMapping("/courses/{courseId}/mentors")
    public Response<List<UserDTO.User>> getMentorsInCourse(@PathVariable(value = "courseId") Long courseId,
                                                           @AuthenticationPrincipal org.springframework.security.core.userdetails.User user){

//        Integer userId = PrincipalUtils.getUserId(user);
        //List<User> mentors = courseService.getMentorsInCourse(courseId);
        List<UserDTO.User> mentors = courseMemberService.findUsersByCourseIdAndRole(courseId, CourseRole.MENTOR);
        return Response.ok(HttpStatus.OK, "수업 정보를 조회하였습니다.",mentors);
    }

//    @PostMapping("/courses/{courseId}/invites")
//    public Response inviteStudentsToCourse(@PathVariable(value = "courseId") Integer courseId, @RequestBody @Valid InviteStudentToCourseDTO inviteStudentToCourseDTO,
//                                           @AuthenticationPrincipal org.springframework.security.core.userdetails.User user){
//
//        Integer userId = PrincipalUtils.getUserId(user);
//        courseService.inviteStudentsToCourse(courseId, inviteStudentToCourseDTO, userId );
//        return Response.success("클래스에 학생들을 초대하였습니다.");
//    }

//    @GetMapping("/courses/{courseId}/participants")
//    public ResponseWithResult<List<ParticipantDTO>> getStudentsParticipants(@PathVariable(value = "courseId") Integer courseId,
//                                                                            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user){
//
//        Integer userId = PrincipalUtils.getUserId(user);
//        List<ParticipantDTO> participants = courseService.getStudentsParticipants(courseId);
//        return ResponseWithResult.success("수강생들의 참여이력을 조회하였습니다.", participants);
//    }

    @GetMapping("/role")
    public Response<Object> getCourseRole(@RequestParam("course") Long courseId,
                                            @AuthenticationPrincipal User user) {

//        Integer userId = PrincipalUtils.getUserId(user);
//        log.info("클래스 권한 조회 : {} in {}", userId, courseId);

        Short role;
        CourseMemberEntity member = courseMemberService.findUserRoleInClass(Long.parseLong(user.getUsername()), courseId);

        Map<String, Short> result = new HashMap();
        result.put("role", member.getRole());

        return Response.ok(HttpStatus.OK, "역할 조회를 성공하였습니다.", result);
    }
}
