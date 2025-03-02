package pheonix.classconnect.backend.com.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pheonix.classconnect.backend.com.auth.entity.AuthorityEntity;

public interface AuthorityRepository extends JpaRepository<AuthorityEntity, Short> {
}
