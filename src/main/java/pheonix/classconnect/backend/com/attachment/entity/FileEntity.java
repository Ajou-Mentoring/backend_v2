package pheonix.classconnect.backend.com.attachment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pheonix.classconnect.backend.com.common.entity.BaseTimeEntity;

@Entity(name="File")
@Table(name = "File")
@Getter
@Builder @NoArgsConstructor @AllArgsConstructor
public class FileEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="domain_type")
    private Short domainType;

    @Column(name="domain_id")
    private Long domainId;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "original_file_name", length = 100, nullable = false)
    private String originalFileName;

    @Column(name = "file_path", nullable = false)
    private String path;

    @Builder.Default
    @Column(name = "size")
    private Long size = 0L;

    @Column(name = "bucket", length = 50)
    private String bucket;  // File Bucket (will be not used)

    @Column(name = "file_type")
    private Short type;  // File Extension

    @Column(name = "upload_state")
    private Short uploadState; // File Upload State

    public void addDomainInfo(Short domain, Long domainId) {
        this.domainType = domain;
        this.domainId = domainId;
    }
}
