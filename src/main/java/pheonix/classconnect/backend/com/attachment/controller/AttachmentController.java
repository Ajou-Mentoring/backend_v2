package pheonix.classconnect.backend.com.attachment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pheonix.classconnect.backend.com.attachment.model.File;
import pheonix.classconnect.backend.com.attachment.service.FileStorage;
import pheonix.classconnect.backend.com.common.model.Response;
import pheonix.classconnect.backend.exceptions.ErrorCode;
import pheonix.classconnect.backend.exceptions.MainApplicationException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/v2")
@RequiredArgsConstructor
@Slf4j
public class AttachmentController {

    private final FileStorage fileStorage;

    @GetMapping("/images/{id}")
    public ResponseEntity<byte[]> showImage (
            @PathVariable("id") Long fileId
    ) throws MalformedURLException {
        log.info("이미지 불러오기, classpath:{}", fileId);
        byte[] image;
        ResponseEntity<byte[]> response = null;
        try {
            File attachment = fileStorage.getFileById(fileId);
            image = fileStorage.getFile(attachment.getPath());
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", Files.probeContentType(Path.of("src", "main", "", "WEB-INF", attachment.getPath())));
            response = new ResponseEntity<>(image, headers, HttpStatus.OK);
        } catch (IOException e){
            throw new MainApplicationException(ErrorCode.FILE_NOT_FOUND, "이미지 조회에 실패하였습니다.");
        }

        return response;
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable("id") Long fileId) {
        log.info("파일 다운로드 : {}", fileId);
        // 파일을 찾는다.
        Resource resource = null;
        String fileName = "";
        try {
            File attachment = fileStorage.getFileById(fileId);
            resource = new ByteArrayResource(fileStorage.getFile(attachment.getPath()));
            fileName = URLEncoder.encode(attachment.getOriginalFileName(), "UTF-8");
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; fileName=\"" + fileName + "\";")
                    .header(HttpHeaders.CONTENT_LENGTH, attachment.getSize() + "")
                    .body(resource);

        } catch (IOException e){
            log.error(e.toString());
            e.printStackTrace();
            throw new MainApplicationException(ErrorCode.FILE_NOT_FOUND, "이미지 조회에 실패하였습니다.");

        }
    }

    @PostMapping(value = "images/upload")
    public Response<Long> uploadImage(@RequestPart(name = "image") MultipartFile image)
    {
        log.info("이미지 업로드 ");

        File stored = fileStorage.saveOnlyFile(image);

        return Response.ok(HttpStatus.CREATED, "파일 업로드에 성공했습니다.", stored.getId());
    }

    @PostMapping(value = "images/upload-all")
    public Response<List<Long>> uploadImages(@RequestPart(name = "images") List<MultipartFile> images)
    {
        log.info("이미지 다중 업로드");

        // 입력값 검증 - 하나 이상의 파일이 업로드 되어야 함
        if (images.isEmpty()) {
            throw new MainApplicationException(ErrorCode.BAK_LOGIC_ERROR, "최소 하나 이상의 파일을 업로드 해야 합니다.");
        }

        List<File> savedImageList = new ArrayList<>();
        for (MultipartFile image : images) {
            try {
                savedImageList.add(fileStorage.saveOnlyFile(image));
            } catch (Exception e) {
                log.info("이미지 저장에 실패했습니다.");
                log.error("cause : {}", e.getMessage());

                // 롤백 : 이미 저장된 이미지 삭제
                if (!savedImageList.isEmpty()) {
                    savedImageList.forEach(img -> {
                        fileStorage.deleteByFileId(img.getId());
                    });
                }
                break;
            }
        }

        return Response.ok(HttpStatus.CREATED, "파일 업로드에 성공했습니다.", savedImageList.stream().map(File::getId).toList());
    }

    @DeleteMapping(value="/file/{id}")
    public Response<String> deleteFile(@AuthenticationPrincipal User user, @PathVariable("id") Long attachmentId) {
        log.info("파일 삭제");

        fileStorage.deleteByFileId(attachmentId);

        return Response.ok("파일을 삭제했습니다.");
    }

}
