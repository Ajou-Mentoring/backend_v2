package pheonix.classconnect.backend.com.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pheonix.classconnect.backend.com.user.entity.UserEntity;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByStudentNo(String studentNo);
    Optional<UserEntity> findByEmail(String email);
}
