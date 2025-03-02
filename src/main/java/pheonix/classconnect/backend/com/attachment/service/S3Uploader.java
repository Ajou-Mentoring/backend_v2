package pheonix.classconnect.backend.com.attachment.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pheonix.classconnect.backend.exceptions.ErrorCode;
import pheonix.classconnect.backend.exceptions.MainApplicationException;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3Uploader {

    private final AmazonS3Client amazonS3Client;

    @Value("${spring.web.resources.static-locations}")
    private String fileUploadDir;

    public String upload(MultipartFile multipartFile, String bucket, String dirName) throws IOException {
        File uploadFile = convert(multipartFile)
                .orElseThrow(() -> new IllegalArgumentException("MultipartFile -> File로 전환이 실패했습니다."));

        return upload(uploadFile, bucket, dirName);
    }

    public void delete(String bucket, String key) {
        deleteS3(bucket, key);
    }

    public ByteArrayOutputStream download(String bucket, String key) {
        log.info("bucket {}, key {}", bucket, key);

        S3Object s3Object = amazonS3Client.getObject(bucket, key);
        try (InputStream inputStream = s3Object.getObjectContent();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private String upload(File uploadFile, String bucket, String dirName) {
        String uuid = UUID.randomUUID() + "_";
        String fileName = dirName + "/" + uuid + uploadFile.getName();
        String uploadImageUrl = putS3(uploadFile, bucket, fileName);
        // 업로드 URL의 파일 명을 디코드
        log.info("URL: {}", uploadImageUrl);
        String[] urlParse = uploadImageUrl.split("_", 2);
        String decodedFileName = urlParse[1];
        try {
            decodedFileName = URLDecoder.decode(urlParse[1], StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        uploadImageUrl = urlParse[0] + "_" + decodedFileName;
        log.info("UPLOAD {}", uploadImageUrl);
        removeNewFile(uploadFile);
        return uploadImageUrl;
    }

    private String putS3(File uploadFile, String bucket, String filePath) {
        try {
            amazonS3Client.putObject(new PutObjectRequest(bucket, filePath, uploadFile));
        } catch (AmazonServiceException e) {
            throw new MainApplicationException(ErrorCode.SYS_INTERNAL_SERVER_ERROR, "클라우드 업로드에 실패했습니다.");
        } catch (SdkClientException e) {
            throw new MainApplicationException(ErrorCode.SYS_INTERNAL_SERVER_ERROR, "SDK 인증에 실패했습니다.");
        }

        return amazonS3Client.getUrl(bucket, filePath).toString();
    }

    private void deleteS3(String bucket, String key) {
        try {
            amazonS3Client.deleteObject(new DeleteObjectRequest(bucket, key));
        } catch (AmazonServiceException e) {
            throw new MainApplicationException(ErrorCode.SYS_INTERNAL_SERVER_ERROR, "클라우드 업로드에 실패했습니다.");
        } catch (SdkClientException e) {
            throw new MainApplicationException(ErrorCode.SYS_INTERNAL_SERVER_ERROR, "SDK 인증에 실패했습니다.");
        }

    }

    private void removeNewFile(File targetFile) {
        if (targetFile.delete()) {
            log.info("파일이 삭제되었습니다.");
        } else {
            log.info("파일이 삭제되지 못했습니다.");
        }
    }

    private Optional<File> convert(MultipartFile file) throws IOException {
        try {
            File directory = new File(fileUploadDir);
            if (!directory.exists()) {
                directory.mkdirs(); // 디렉토리가 없을 경우 생성
            }

            File pendingFile = new File(fileUploadDir + file.getOriginalFilename());
            FileOutputStream fos = new FileOutputStream(fileUploadDir + file.getOriginalFilename());
            fos.write(file.getBytes());
            fos.close();

            return Optional.of(pendingFile);
        } catch (IOException e) {
            log.error("Failed to store file {}, {}", file.getOriginalFilename(), e.getMessage());
            return Optional.empty();
        }
    }

    // https://jojoldu.tistory.com/300 참조
}
