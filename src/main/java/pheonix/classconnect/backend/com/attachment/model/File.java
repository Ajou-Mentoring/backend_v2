package pheonix.classconnect.backend.com.attachment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import pheonix.classconnect.backend.com.attachment.entity.FileEntity;

@Getter @Setter
@AllArgsConstructor
@Builder
public class File {
    private Long id;  // File ID

    private Short attachedIn;  // File Domain Type

    private Long attachedTo;

    private String name;

    private String originalFileName;

    private String bucket;

    private String path;

    private Long size;

    private Short type;

    private Short uploadState;  // File Upload State

    public static File fromEntity(FileEntity entity) {
        return File.builder()
                .id(entity.getId())
                .attachedIn(entity.getDomainType())
                .attachedTo(entity.getDomainId())
                .name(entity.getName())
                .originalFileName(entity.getOriginalFileName())
                .bucket(entity.getBucket())
                .path(entity.getPath())
                .size(entity.getSize())
                .type(entity.getType())
                .uploadState(entity.getUploadState())
                .build();
    }
}
