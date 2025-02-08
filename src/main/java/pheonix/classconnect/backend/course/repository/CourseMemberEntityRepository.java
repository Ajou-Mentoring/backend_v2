package pheonix.classconnect.backend.course.repository;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pheonix.classconnect.backend.course.entity.CourseMemberEntity;
import pheonix.classconnect.backend.course.model.SemesterMapper;

import java.util.List;
import java.util.Optional;

public interface CourseMemberEntityRepository extends JpaRepository<CourseMemberEntity, Integer> {

    @Query("SELECT uce FROM CourseMember uce WHERE uce.user.id = :userId AND uce.course.year = :year AND uce.course.semester = :semester")
    List<CourseMemberEntity> findByUserIdAndCourseYearAndCourseSemester(@Param("userId") Long userId, @Param("year") String year, @Param("semester") Short semester);

    @Query("SELECT DISTINCT uce.course.year, uce.course.semester FROM CourseMember uce WHERE uce.user.id = :userId order by uce.course.year desc, uce.course.semester desc")
    List<SemesterMapper> findDistinctSemestersByUserId(@Param("userId") Long userId);

    List<CourseMemberEntity> findAllByCourseIdAndRole(Long courseId, Short role);

    boolean existsByUserIdAndRole(Long userId, Short role);

    Optional<CourseMemberEntity> findByUserIdAndCourseId(Long userId, Long courseId);

    List<CourseMemberEntity> findByCourseIdAndRole(Long courseId, Short role);

    @Modifying
    @Query("DELETE FROM CourseMember uc WHERE uc.course.id = :courseId AND uc.user.id IN :memberIds")
    void deleteMembersFromCourse(@Param("courseId") Long courseId, @Param("memberIds") List<Long> memberIds);

    List<CourseMemberEntity> findByUserIdOrderByCourseYearDescCourseSemesterDesc(@NotNull(message = "멤버 아이디를 입력해주세요.") Long memberId);


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