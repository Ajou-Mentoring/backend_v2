package pheonix.classconnect.backend.com.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import pheonix.classconnect.backend.com.attachment.constants.AttachmentDomainType;
import pheonix.classconnect.backend.com.attachment.model.File;
import pheonix.classconnect.backend.com.attachment.model.response.FileResponse;
import pheonix.classconnect.backend.com.attachment.service.FileStorage;
import pheonix.classconnect.backend.com.auth.model.AuthorityDTO;
import pheonix.classconnect.backend.com.common.model.Paged;
import pheonix.classconnect.backend.com.common.model.Response;
import pheonix.classconnect.backend.com.user.model.UserDTO;
import pheonix.classconnect.backend.com.user.service.UserService;
import pheonix.classconnect.backend.exceptions.ErrorCode;
import pheonix.classconnect.backend.exceptions.MainApplicationException;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v2")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final FileStorage fileStorage;

    @GetMapping("/users/me/profile")
    public Response<UserDTO.Response00> getUserProfile(@AuthenticationPrincipal User user) {
        log.info("UserController.getUserProfile()");

        // 요청 검증
        if (user == null) {
            throw new MainApplicationException(ErrorCode.BACK_INVALID_PERMISSION, "사용자 권한 정보가 없습니다.");
        }

        UserDTO.User usr = userService.findUserById(Long.parseLong(user.getUsername()));

        // 프로필 세팅
        List<File> files = fileStorage.getAttachmentList(AttachmentDomainType.PROFILE, usr.getId());
        FileResponse.Info profileImg = files.isEmpty() ? new FileResponse.Info() : FileResponse.Info.fromFile(files.getFirst());

        // 응답 SET
        UserDTO.Response00 res = UserDTO.Response00.builder()
                .id(usr.getId())
                .name(usr.getName())
                .email(usr.getEmail())
                .studentNo(usr.getStudentNo())
                .department(usr.getDepartment().getName())
                .auth(usr.getAuthorities().stream()
                        .map(AuthorityDTO.AuthorityInfo::getCode)
                        .max(Short::compare)
                        .orElse((short) 0))
                .build();

        res.setProfile(profileImg);

        return Response.ok(HttpStatus.OK, "프로필을 조회했습니다.", res);
    }

    @GetMapping("/users")
    public Response<Paged<UserDTO.Response05>> getUserPage(@RequestParam(value = "searchBy", required = false) String searchBy,
                                                            @RequestParam(value = "clue", required = false) String clue,
                                                            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                                                            @RequestParam(value = "page", defaultValue = "20", required = false) int size,
                                                            @AuthenticationPrincipal User user) {
        log.info("UserController.getUserPage({}, {}, {}, {})", searchBy, clue, page, size);

        // 요청 검증
        if (user == null) {
            throw new MainApplicationException(ErrorCode.BACK_INVALID_PERMISSION, "사용자 권한 정보가 없습니다.");
        }

        Paged<UserDTO.User> users;
        // 전체 조회
        if (searchBy == null && clue == null) {
            users = userService.findUsers(page, size);
        }
        // 이름으로 조회
        else if (searchBy != null && clue != null && !clue.isBlank() && searchBy.equalsIgnoreCase("name")) {
            users = userService.findUsersByName(clue, page, size);
        }
        // 학번으로 조회
        else if (searchBy != null && clue != null && !clue.isBlank() && searchBy.equalsIgnoreCase("studentNo")) {
            users = userService.findUsersByStudentNo(clue, page, size);
        }
        else {
            throw new MainApplicationException(ErrorCode.USER_BAD_REQUEST, "지원하지 않는 조회 구분입니다.");
        }

        // 응답 SET
        Paged<UserDTO.Response05> res = Paged.<UserDTO.Response05>builder()
                .currentPage(users.getCurrentPage())
                .numberOfElements(users.getNumberOfElements())
                .size(users.getSize())
                .totalPages(users.getTotalPages())
                .totalElements(users.getTotalElements())
                .items(users.getItems().stream()
                        .map(usr -> UserDTO.Response05.builder()
                                .id(usr.getId())
                                .name(usr.getName())
                                .email(usr.getEmail())
                                .studentNo(usr.getStudentNo())
                                .department(usr.getDepartment().getName())
                                .auth(usr.getAuthorities().stream()
                                        .map(AuthorityDTO.AuthorityInfo::getCode)
                                        .max(Short::compare)
                                        .orElse((short) 0))
                                .build())
                        .toList())
                .build();

        return Response.ok(HttpStatus.OK, "사용자 페이지를 조회했습니다.", res);
    }

//    // 회원가입
//    @PostMapping("/users/sign-up")
//    public Response<UserDTO.User> addNewUser(@RequestBody @Valid UserDTO.Signup signupDTO) {
//        log.info("회원 등록");
//
//        // 이미 가입된 사용자인지 체크
//        UserDTO.FindOne findDto = new UserDTO.FindOne();
//        if (userService.findOne() != null) {
//            throw new MainApplicationException(ErrorCode.DUPLICATED_USER, "이미 가입된 사용자입니다.");
//        }
//
//        log.info("{} {} {}", signupDTO.getStudentCode(), signupDTO.getEmail(), signupDTO.getName());
//
//        UserRole role = signupDTO.getRole() == null || signupDTO.getRole().equals("STUDENT") ? UserRole.STUDENT : UserRole.PROFESSOR;
//
//        UserEntity userEntity = UserEntity.builder()
//                .nickname(signupDTO.getName())
//                .email(signupDTO.getEmail())
//                .role(role)
//                .build();
//
//        // 권한 추가
//        userEntity.gainRoles(roleService.rolesOf(role));
//        log.info("추가된 권한 : {} -> {}", userEntity.getRoles().stream().map(RoleEntity::toString).toList().get(0), userEntity.getEmail());
//        UserProfileEntity userProfile = new UserProfileEntity();
//        userProfile.setCode(signupDTO.getStudentCode());
//        userProfile.setDepartment(departmentService.getOrSaveDepartmentByName(signupDTO.getDepartment()));
//
//        userProfile.setUser(userEntity);
//        userEntity.setUserProfile(userProfile);
//
//        // 권한 세팅
//        userEntity.setNotificationSetting(new NotificationSettingEntity(userEntity));
//        UserEntity newUser = userService.createUser(userEntity);
//
//        // invite 객체 조회 & 존재하면 UserCourseEntity 생성
//        courseInviteService.findUserInvitesByEmail(newUser.getId(), signupDTO.getEmail());
//
//
//
//        return ResponseWithResult.success("회원가입 성공", User.fromEntity(newUser));
}
