package pheonix.classconnect.backend.com.department.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pheonix.classconnect.backend.com.department.entity.DepartmentEntity;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<DepartmentEntity, Integer> {
    Optional<DepartmentEntity> findByName(String name);
    Optional<Integer> findTopByOrderByIdDesc();
}
