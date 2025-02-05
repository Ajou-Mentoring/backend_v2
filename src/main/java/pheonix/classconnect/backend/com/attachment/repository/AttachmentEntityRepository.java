package pheonix.classconnect.backend.com.attachment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pheonix.classconnect.backend.com.attachment.entity.FileEntity;

import java.util.List;

@Repository
public interface AttachmentEntityRepository extends JpaRepository<FileEntity, Long> {
    List<FileEntity> findAllByDomainTypeAndDomainId(Short domainType, Long domainId);
}
