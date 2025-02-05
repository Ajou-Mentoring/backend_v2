package pheonix.classconnect.backend.com.attachment.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pheonix.classconnect.backend.com.attachment.constants.AttachmentFileType;
import pheonix.classconnect.backend.com.attachment.constants.AttachmentUploadState;
import pheonix.classconnect.backend.com.attachment.entity.FileEntity;
import pheonix.classconnect.backend.com.attachment.model.File;
import pheonix.classconnect.backend.com.attachment.repository.AttachmentEntityRepository;
import pheonix.classconnect.backend.exceptions.ErrorCode;
import pheonix.classconnect.backend.exceptions.MainApplicationException;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageImpl implements FileStorage {

    private final AttachmentEntityRepository attachmentEntityRepository;
    private final S3Uploader s3Uploader;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;


    @Override
    public File saveFile(MultipartFile file, Short domain, Long domainId) {
        // 파일 저장
        FileEntity created = saveToFileSystem(file);
        log.info(file.getContentType(), file.getOriginalFilename());

        created.addDomainInfo(domain, domainId);

        // 엔티티 저장
        FileEntity savedEntity = attachmentEntityRepository.save(created);

        return File.fromEntity(savedEntity);
    }

    @Override
    public File saveOnlyFile(MultipartFile file) {
        log.info("파일만 저장 : {}", file.getOriginalFilename());
        // 파일 저장
        FileEntity created = saveToFileSystem(file);

        // 엔티티 저장
        FileEntity savedEntity = attachmentEntityRepository.save(created);

        return File.fromEntity(savedEntity);
    }

    // MultipartFile을
    @Override
    public FileEntity saveToFileSystem(MultipartFile file) {

        String filePathByType;
        Short fileType = checkAttachmentFileType(file);
        if (isImage(fileType)) {
            filePathByType = "Images";
        } else if (isScript(fileType)) {
            filePathByType = "Scripts";
        } else {
            throw new MainApplicationException(ErrorCode.UNSUPPORTED_FILE);
        }

        String fileUploadBasePath = "user";
        String key = Path.of(fileUploadBasePath, filePathByType, String.valueOf(LocalDate.now().getYear()), String.valueOf(LocalDate.now().getMonthValue())).toString();


        // 파일 저장
        try {
            String filePath = s3Uploader.upload(file, bucket, key);

            return FileEntity.builder()
                    .id(null)
                    .name(filePath.substring(filePath.lastIndexOf('/')+1))
                    .originalFileName(file.getOriginalFilename())
                    .path(filePath)
                    .bucket(bucket)
                    .size(file.getSize())
                    .type(fileType)
                    .uploadState(AttachmentUploadState.READY)
                    .build();

        } catch (IOException e) {
            throw new MainApplicationException(ErrorCode.SYS_INTERNAL_SERVER_ERROR, "파일 업로드 실패");
        }

    }

    @Override
    public File getFileById(Long Id) {
        log.info("Attachment {} 조회", Id);
        FileEntity entity = attachmentEntityRepository.findById(Id).orElseThrow(
                () -> new MainApplicationException(ErrorCode.FILE_NOT_FOUND, "파일이 존재하지 않습니다."));

        return File.fromEntity(entity);
    }

    public void deleteAllFilesIn(@NotNull Short domainType, Long domainId) {
        log.info("{} {}에 등록된 모든 파일 삭제", domainType, domainId);

        try {
            for (FileEntity file : attachmentEntityRepository.findAllByDomainTypeAndDomainId(domainType, domainId)) {
                deleteByFileId(file.getId());
            }
        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    @Override
    public void deleteByFileId(Long id) {
        FileEntity toDelete = attachmentEntityRepository.findById(id)
                .orElseThrow(() -> new MainApplicationException(ErrorCode.FILE_NOT_FOUND, String.format("삭제할 파일을 찾을 수 없습니다. [%d]", id)));

        log.info("Attachment 삭제 : {} ", toDelete.getPath());

        // 파일 삭제
        String filePath = toDelete.getPath();

        String bucket = filePath.split("\\.")[0].substring(8); // https:// 이후 추출
        String key = filePath.split(".amazonaws.com/")[1];
        try {
            // 파일을 물리적으로 삭제
            s3Uploader.delete(bucket, key);
            // DB에 저장된 파일 기록을 삭제
            attachmentEntityRepository.delete(toDelete);
        } catch (MainApplicationException e) {
            log.error("파일 삭제에 실패했습니다. {}", toDelete.getName());
        }

        // DB에 저장된 파일 매핑 정보를 삭제
        // Cascade로 해결
    }

    @Override
    public byte[] getFile(String filePath) throws IOException {

        String bucket = filePath.split("\\.")[0].substring(8); // https:// 이후 추출
        String key = filePath.split(".amazonaws.com/")[1];

        return s3Uploader.download(bucket, key).toByteArray();
    }

    public List<File> getAttachmentList(@NotNull Short domain, Long domainId) {
        return attachmentEntityRepository.findAllByDomainTypeAndDomainId(domain, domainId)
                .stream()
                .map(File::fromEntity)
                .toList();
    }

    @Override
    public boolean isImage(@NotNull Short fileType) {
        for (Short imageType : List.of(AttachmentFileType.JPG, AttachmentFileType.JPEG, AttachmentFileType.PNG, AttachmentFileType.HEIC)) {
            if (imageType.equals(fileType))
                return true;
        }
        return false;
    }

    @Override
    public boolean isScript(@NotNull Short scriptType) {
        // 잘못된 파일 명 제외
        // 2024.04.26 : 현재 AttachmentFileType에 있는 모든 파일 타입 허가
        return true;
    }

    public File mapAttachmentToItem(Long attachmentId, @NotNull Short domain, Long domainId) {
        log.info("Attachment 매핑 : Attachment {} In {} To {}", attachmentId, domain, domainId);

        FileEntity file = attachmentEntityRepository.findById(attachmentId).orElseThrow(() -> new MainApplicationException(ErrorCode.FILE_NOT_FOUND, "File Not Found"));
        file.addDomainInfo(domain, domainId);

        return File.fromEntity(file);
    }

    private Short checkAttachmentFileType(MultipartFile file) {
        String fileContentType = file.getContentType();
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || !originalFileName.contains(".")) {
            throw new MainApplicationException(ErrorCode.UNSUPPORTED_FILE, "잘못된 형식의 파일");
        }

        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
        log.info("확장자 : {} {}", originalFileName, fileExtension);
        log.info("content type = {}, file name = {}", fileContentType, originalFileName);

        switch (fileExtension) {
            case "jpg": return AttachmentFileType.JPG;
            case "jpeg": return AttachmentFileType.JPEG;
            case "png": return AttachmentFileType.PNG;
            case "pdf": return AttachmentFileType.PDF;
            case "c": return AttachmentFileType.C;
            case "java": return AttachmentFileType.JAVA;
            case "python": return AttachmentFileType.PYTHON;
            case "docx": return AttachmentFileType.DOCX;
            case "csv": return AttachmentFileType.CSV;
        }

        throw new MainApplicationException(ErrorCode.UNSUPPORTED_FILE, "지원되지 않는 파일 타입");
    }
}
