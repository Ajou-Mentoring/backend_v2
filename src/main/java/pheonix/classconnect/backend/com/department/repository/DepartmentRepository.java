package pheonix.classconnect.backend.com.department.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pheonix.classconnect.backend.com.department.entity.DepartmentEntity;

public interface DepartmentRepository extends JpaRepository<DepartmentEntity, Integer> {
}
