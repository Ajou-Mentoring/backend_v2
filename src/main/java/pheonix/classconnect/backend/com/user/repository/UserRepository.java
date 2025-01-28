package pheonix.classconnect.backend.com.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pheonix.classconnect.backend.com.user.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
