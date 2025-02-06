package pheonix.classconnect.backend.course.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pheonix.classconnect.backend.course.entity.ProfessorEntity;

import java.util.List;
import java.util.Optional;

public interface ProfessorRepository extends JpaRepository<ProfessorEntity, Long> {
    Optional<ProfessorEntity> findByName(String name);
}
