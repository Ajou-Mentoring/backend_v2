package pheonix.classconnect.backend.com.attachment.service;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;
import pheonix.classconnect.backend.com.attachment.entity.FileEntity;
import pheonix.classconnect.backend.com.attachment.model.File;

import java.io.IOException;
import java.util.List;

public interface FileStorage {
    File saveFile(MultipartFile file, Short domain, Long domainId);
    File saveOnlyFile(@NotNull MultipartFile file);
    FileEntity saveToFileSystem(MultipartFile file);
    File getFileById(Long Id);
    void deleteByFileId(Long id);
    void deleteAllFilesIn(Short domainType, Long domainId);
    byte[] getFile(String filePath) throws IOException;
    List<File> getAttachmentList(Short domain, Long domainId);
    boolean isImage(@NotNull Short fileType);
    boolean isScript(@NotNull Short scriptType);
    void changeImages(Short domainType, Long domainId, List<Long> updated);
    void changeFiles(Short domainType, Long domainId, List<Long> updated);

    File mapFileToDomain(Long attachmentId, Short domain, Long domainId);
}
