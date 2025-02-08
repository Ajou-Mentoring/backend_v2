package pheonix.classconnect.backend.course.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pheonix.classconnect.backend.course.entity.CourseEntity;

import java.util.List;
import java.util.Optional;

public interface CourseEntityRepository extends JpaRepository<CourseEntity, Long> {
//    List<CourseEntity> findAllByProfessorIdAndYearAndSemester(Long professorId, String year, Short semester);

//    @Query("SELECT distinct ce.year, ce.semester FROM Course ce WHERE ce.professor.id = :userId order by ce.year desc, ce.semester desc")
//    List<Object>  findDistinctSemestersByUserId(@Param("userId") Long userId);

    List<CourseEntity> findAllByYearAndSemester(String year, Short semester);

    Optional<CourseEntity> findBySemesterAndYearAndCode(Short semester, String year, String courseCode);

    Page<CourseEntity> findByYearAndSemester(String year, Short convertedSemester, Pageable pageable);

    @Query("SELECT DISTINCT c.year, c.semester FROM Course c")
    List<Object> findDistinctSemesters();
}
