package pheonix.classconnect.backend.com.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pheonix.classconnect.backend.com.user.entity.UserEntity;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByStudentNo(String studentNo);
    Optional<UserEntity> findByEmail(String email);
    Page<UserEntity> findAllByNameContainsIgnoreCase(String keyword, Pageable pageable);
    Page<UserEntity> findAllByStudentNoContains(String keyword, Pageable pageable);


    @Query("SELECT u FROM User u " +
                "JOIN FETCH u.department d " +
                "LEFT JOIN FETCH u.authorities a " +
                "WHERE u.id = :id")
    Optional<UserEntity> findUserInfoById(@Param("id") Long id);

}
