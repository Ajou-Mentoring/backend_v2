package pheonix.classconnect.backend.course.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pheonix.classconnect.backend.com.user.entity.UserEntity;
import pheonix.classconnect.backend.com.user.model.UserDTO;
import pheonix.classconnect.backend.com.user.repository.UserRepository;
import pheonix.classconnect.backend.course.constants.CourseRole;
import pheonix.classconnect.backend.course.constants.CourseStatus;
import pheonix.classconnect.backend.course.entity.CourseEntity;
import pheonix.classconnect.backend.course.entity.CourseMemberEntity;
import pheonix.classconnect.backend.course.entity.UserCourseId;
import pheonix.classconnect.backend.course.model.CourseDTO;
import pheonix.classconnect.backend.course.model.request.UpdateMemberRoleDTO;
import pheonix.classconnect.backend.course.repository.CourseEntityRepository;
import pheonix.classconnect.backend.course.repository.CourseMemberEntityRepository;
import pheonix.classconnect.backend.exceptions.ErrorCode;
import pheonix.classconnect.backend.exceptions.MainApplicationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseMemberService {
    private final CourseMemberEntityRepository courseMemberEntityRepository;
    private final UserRepository userRepository;
    private final CourseEntityRepository courseEntityRepository;

    public boolean isUserMentorInAnyClass(Long userId){
            return courseMemberEntityRepository.existsByUserIdAndRole(userId, CourseRole.MENTOR);
    }

    public boolean isCourseMember(Long courseId, Long userId) {
        log.info("코스 멤버 여부 체크");
        // 코스가 존재하지 않을 경우
        courseEntityRepository.findById(courseId)
                .orElseThrow(() -> new MainApplicationException(ErrorCode.COURSE_NOT_FOUND, String.format("코스 정보가 없습니다. [%d]", courseId)));

        // 유저가 존재하지 않을 경우
        userRepository.findById(userId)
                .orElseThrow(() -> new MainApplicationException(ErrorCode.USER_NOT_FOUND, String.format("유저 정보가 없습니다. [%d]", userId)));

        return courseMemberEntityRepository.findByUserIdAndCourseId(userId, courseId).isPresent();
    }

    public CourseMemberEntity findUserRoleInClass(Long userId, Long courseId){
        return courseMemberEntityRepository.findByUserIdAndCourseId(userId, courseId).orElseThrow(() ->
            new MainApplicationException(ErrorCode.COURSE_MEMBER_NOT_FOUND, String.format("userId %s is not in class which id is %s", userId, courseId))
        );
    }

    public Short findMemberRoleInClass(Long userId, Long courseId) {
        return courseMemberEntityRepository.findByUserIdAndCourseId(userId, courseId)
                .map(CourseMemberEntity::getRole).orElse((short) 0);
    }

    public List<UserDTO.User> findUsersByCourseIdAndRole(Long courseId, Short role) {
        List<CourseMemberEntity> members = courseMemberEntityRepository.findAllByCourseIdAndRole(courseId, role);
        if (members.isEmpty())
            return new ArrayList<>();
        else {
            return members.stream()
                    .map(CourseMemberEntity::getUser)
                    .map(UserDTO.User::fromEntity).toList();
        }
    }



    public List<CourseMemberEntity> findByUserIdAndCourseYearAndCourseSemester(Long userId, String year, Short semester){
        return courseMemberEntityRepository.findByUserIdAndCourseYearAndCourseSemester(userId, year, semester);
    }


    @Transactional
    public void saveAll(List<CourseMemberEntity> userCourseEntities) {
//        for (UserCourseEntity uc : userCourseEntities) {
//            log.info("uc {} {} {}", uc.getCourse().getId(), uc.getCourse().getCourseCode(), uc.getUser().getId());
//            userCourseEntityRepository.save(uc);
//        }
         courseMemberEntityRepository.saveAll(userCourseEntities);
    }

    public void excludeMemberFromCourse(Long courseId, Long userId) {
        // 입력값 검증
        CourseMemberEntity member = courseMemberEntityRepository.findByUserIdAndCourseId(courseId, userId)
                .orElseThrow(() -> new MainApplicationException(ErrorCode.COURSE_MEMBER_NOT_FOUND, String.format("멤버를 찾을 수 없습니니다. 코스 : [%d]  유저 : [%d]", courseId, userId)));

        courseMemberEntityRepository.delete(member);
    }

    public void updateMemberRoleInCourse(Long courseId, Long userId, Short role) {
        log.info("코스 역할 변경 : {} - {} => {}}", courseId, userId, role);

        // 요청 검증
        CourseEntity course = courseEntityRepository.findById(courseId)
                .orElseThrow(() -> new MainApplicationException(ErrorCode.COURSE_NOT_FOUND, "코스 정보를 찾을 수 없습니다."));
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new MainApplicationException(ErrorCode.USER_NOT_FOUND, "유저 정보를 찾을 수 없습니다."));

        CourseMemberEntity courseMember = courseMemberEntityRepository.findByUserIdAndCourseId( user.getId(), course.getId()).orElseThrow(
                () -> new MainApplicationException(ErrorCode.COURSE_MEMBER_NOT_FOUND, "해당 유저는 코스 멤버가 아닙니다."));

        if (Objects.equals(courseMember.getRole(), role)) {
            throw new MainApplicationException(ErrorCode.COURSE_MEMBER_BAD_REQUEST, "동일한 역할로 변경할 수 없습니다.");
        }
        courseMember.setRole(role);
        courseMemberEntityRepository.save(courseMember);
    }

    public List<UserDTO.User> findMentors(Long courseId) {
        log.info("findMentors()");

        // 2. courseId != 0 이면 해당 코스의 멘토 정보 조회
        return courseMemberEntityRepository.findAllByCourseIdAndRole(courseId, CourseRole.MENTOR).stream()
                .map(CourseMemberEntity::getUser)
                .map(UserDTO.User::fromEntity)
                .toList();

    }

    public void includeMemberToCourse(Long userId, @Valid String memberCode) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new MainApplicationException(ErrorCode.USER_NOT_FOUND, String.format("사용자를 찾을 수 없습니다. [%d]", userId)));

        CourseEntity course = courseEntityRepository.findByMemberCode(memberCode)
                .orElseThrow(() -> new MainApplicationException(ErrorCode.COURSE_NOT_FOUND, String.format("해당 참여코드로 조회된 코스가 없습니다. [%s]", memberCode)));

        if (courseMemberEntityRepository.findByUserIdAndCourseId(userId, course.getId()).isPresent()) {
            throw new MainApplicationException(ErrorCode.DUPLICATED_COURSE_MEMBER, String.format("이미 코스에 가입했습니다. [%d] -> [%d]", userId, course.getId()));
        }

        if (!Objects.equals(course.getStatus(), CourseStatus.OPEN)) {
            throw new MainApplicationException(ErrorCode.COURSE_NOT_OPEN, "현재 코스 참가 가능한 상태가 아닙니다.");
        }

        CourseMemberEntity member = CourseMemberEntity.builder()
                .course(course)
                .user(user)
                .role(CourseRole.MENTEE)
                .id(new UserCourseId(userId, course.getId()))
                .build();

        courseMemberEntityRepository.save(member);
    }

    public List<CourseDTO.Member> findMembersInCourse(@Valid Long courseId) {
        log.info("{} 코스 내 멤버 조회", courseId);

        // 입력값 검증
        courseEntityRepository.findById(courseId)
                .orElseThrow(() -> new MainApplicationException(ErrorCode.COURSE_NOT_FOUND, "코스를 찾을 수 없습니다."));

        // 본처리
        return courseMemberEntityRepository.findAllByCourseId(courseId).stream()
                .map(CourseDTO.Member::fromEntity)
                .toList();
    }


//    public List<User> getUsersForNewPostNotification(Integer courseId, boolean isMentorOrProfessor) {
//        return userCourseEntityRepository.findUsersWithNewPostNotificationEnabled(courseId, isMentorOrProfessor).stream().map(User::fromEntity).collect(Collectors.toList());
//    }
}
