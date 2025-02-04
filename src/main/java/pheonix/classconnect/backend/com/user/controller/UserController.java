package pheonix.classconnect.backend.com.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pheonix.classconnect.backend.com.common.model.Response;
import pheonix.classconnect.backend.com.user.model.UserDTO;
import pheonix.classconnect.backend.com.user.service.UserService;

@Slf4j
@RestController
@RequestMapping("/v2")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

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
