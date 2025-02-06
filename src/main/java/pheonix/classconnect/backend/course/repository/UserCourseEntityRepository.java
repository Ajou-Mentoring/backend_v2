package pheonix.classconnect.backend.course.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pheonix.classconnect.backend.com.user.entity.UserEntity;
import pheonix.classconnect.backend.course.entity.UserCourseEntity;

import java.util.List;
import java.util.Optional;

public interface UserCourseEntityRepository extends JpaRepository<UserCourseEntity, Integer> {

    @Query("SELECT uce FROM UserCourse uce WHERE uce.user.id = :userId AND uce.course.year = :year AND uce.course.semester = :semester")
    List<UserCourseEntity> findByUserIdAndCourseYearAndCourseSemester(@Param("userId") Long userId, @Param("year") String year, @Param("semester") Short semester);

    @Query("SELECT DISTINCT uce.course.year, uce.course.semester FROM UserCourse uce WHERE uce.user.id = :userId order by uce.course.year desc, uce.course.semester desc")
    List<SemesterMapper> findDistinctSemestersByUserId(@Param("userId") Long userId);

    List<UserCourseEntity> findAllByCourseIdAndRole(Long courseId, Short role);

    boolean existsByUserIdAndRole(Long userId, Short role);

    Optional<UserCourseEntity> findByUserIdAndCourseId(Long userId, Long courseId);

    List<UserCourseEntity> findByCourseIdAndRole(Long courseId, Short role);

    @Modifying
    @Query("DELETE FROM UserCourse uc WHERE uc.course.id = :courseId AND uc.user.id IN :memberIds")
    void deleteMembersFromCourse(@Param("courseId") Long courseId, @Param("memberIds") List<Long> memberIds);


//    @Query("SELECT DISTINCT uc.user FROM UserCourse uc " +
//            "JOIN uc.user u " +
//            "JOIN u.notificationSetting ns " +
//            "WHERE uc.course.id = :courseId " +
//            "AND ns.isNewPost = true " +
//            "AND (:isMentorOrProfessor = false OR uc.role IN ('MENTOR', 'PROFESSOR'))")
//    List<UserEntity> findUsersWithNewPostNotificationEnabled(
//            @Param("courseId") Integer courseId,
//            @Param("isMentorOrProfessor") boolean isMentorOrProfessor
//    );




}