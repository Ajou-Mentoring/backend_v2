package pheonix.classconnect.backend.course.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pheonix.classconnect.backend.com.user.entity.UserEntity;
import pheonix.classconnect.backend.com.user.model.UserDTO;
import pheonix.classconnect.backend.course.constants.CourseRole;
import pheonix.classconnect.backend.course.entity.CourseEntity;
import pheonix.classconnect.backend.course.entity.UserCourseEntity;
import pheonix.classconnect.backend.course.model.request.RemoveMemberFromCourseDTO;
import pheonix.classconnect.backend.course.model.request.UpdateMemberRoleDTO;
import pheonix.classconnect.backend.course.repository.UserCourseEntityRepository;
import pheonix.classconnect.backend.exceptions.ErrorCode;
import pheonix.classconnect.backend.exceptions.MainApplicationException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCourseService {
    private final UserCourseEntityRepository userCourseEntityRepository;

    public boolean isUserMentorInAnyClass(Long userId){
            return userCourseEntityRepository.existsByUserIdAndRole(userId, CourseRole.MENTOR);
    }

//    public boolean isUserInCourse(UserEntity user, CourseEntity course) {
//        // 교수일 경우
////        if (course.getProfessor().equals(user))
////            return true;
//        return  userCourseEntityRepository.findByUserIdAndCourseId(user.getId(), )
////        return user.getCourses().stream()
////                .map(UserCourseEntity::getCourse)
////                .anyMatch(c -> c != null && c.getId().equals(course.getId()));
//    }

    public UserCourseEntity findUserRoleInClass(Long userId, Long courseId){
        return userCourseEntityRepository.findByUserIdAndCourseId(userId, courseId).orElseThrow(() ->
            new MainApplicationException(ErrorCode.COURSE_MEMBER_NOT_FOUND, String.format("userId %s is not in class which id is %s", userId, courseId))
        );
    }

    public List<UserDTO.User> findUsersByCourseIdAndRole(Long courseId, Short role) {
        List<UserCourseEntity> members = userCourseEntityRepository.findByCourseIdAndRole(courseId, role);
        if (members.isEmpty())
            return new ArrayList<>();
        else {
            return members.stream()
                    .map(UserCourseEntity::getUser)
                    .map(UserDTO.User::fromEntity).toList();
        }
    }



    public List<UserCourseEntity> findByUserIdAndCourseYearAndCourseSemester(Long userId, String year, Short semester){
        return userCourseEntityRepository.findByUserIdAndCourseYearAndCourseSemester(userId, year, semester);
    }


    @Transactional
    public void saveAll(List<UserCourseEntity> userCourseEntities) {
//        for (UserCourseEntity uc : userCourseEntities) {
//            log.info("uc {} {} {}", uc.getCourse().getId(), uc.getCourse().getCourseCode(), uc.getUser().getId());
//            userCourseEntityRepository.save(uc);
//        }
         userCourseEntityRepository.saveAll(userCourseEntities);
    }

    public void removeMemberFromCourse(CourseEntity course, RemoveMemberFromCourseDTO removeMemberFromCourseDTO) {
        List<Long> memberIds = removeMemberFromCourseDTO.getMemberIds();
        if(!memberIds.isEmpty()){
            userCourseEntityRepository.deleteMembersFromCourse(course.getId(), memberIds);
        }
    }

    public void updateMemberRoleInCourse(CourseEntity course, UserEntity user, UpdateMemberRoleDTO updateMemberRoleDTO) {
        UserCourseEntity courseMember = userCourseEntityRepository.findByUserIdAndCourseId( user.getId(), course.getId()).orElseThrow(
                () -> new MainApplicationException(ErrorCode.COURSE_MEMBER_NOT_FOUND, "해당 유저가 클래스내에 존재하지 않습니다."));
        courseMember.setRole(updateMemberRoleDTO.getRole());
        userCourseEntityRepository.save(courseMember);
    }


//    public List<User> getUsersForNewPostNotification(Integer courseId, boolean isMentorOrProfessor) {
//        return userCourseEntityRepository.findUsersWithNewPostNotificationEnabled(courseId, isMentorOrProfessor).stream().map(User::fromEntity).collect(Collectors.toList());
//    }
}
