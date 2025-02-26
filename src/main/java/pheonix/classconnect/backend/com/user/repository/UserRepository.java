package pheonix.classconnect.backend.com.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pheonix.classconnect.backend.com.user.entity.UserEntity;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByStudentNo(String studentNo);
    Optional<UserEntity> findByEmail(String email);
    Page<UserEntity> findAllByNameContainsIgnoreCase(String keyword, Pageable pageable);
    Page<UserEntity> findAllByStudentNoContains(String keyword, Pageable pageable);
}
