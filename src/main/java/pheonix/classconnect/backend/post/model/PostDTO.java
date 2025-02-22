package pheonix.classconnect.backend.post.model;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import pheonix.classconnect.backend.com.attachment.model.File;
import pheonix.classconnect.backend.com.attachment.model.response.FileResponse;
import pheonix.classconnect.backend.com.user.model.UserDTO;
import pheonix.classconnect.backend.post.entity.PostEntity;

import java.time.LocalDateTime;
import java.util.List;

public class PostDTO {

    @Data
    @Builder
    public static class Post {
        private Long id;
        private String title;
        private String content;
        private Short postType;
        private Short uploadStatus;
        private Short publishType;
        private UserDTO.User writer;
        private List<File> images;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Post fromEntity(PostEntity entity) {
            return Post.builder()
                    .id(entity.getId())
                    .title(entity.getTitle())
                    .content(entity.getContent())
                    .postType(entity.getPostType())
                    .uploadStatus(entity.getUploadStatus())
                    .publishType(entity.getPublishType())
                    .writer(UserDTO.User.fromEntity(entity.getWriter()))
                    .createdAt(LocalDateTime.of(entity.getCreatedDate(), entity.getCreatedTime()))
                    .updatedAt(LocalDateTime.of(entity.getUpdatedDate(), entity.getUpdatedTime()))
                    .build();
        }

    }
    @Data
    @Builder
    public static class Create {
        private String title;
        private String content;
        private Short postType;
        private Short uploadStatus;
        private Short publishType;
        private Long writerId;
    }

    @Data
    @Builder
    public static class Update {
        private String title;
        private String content;
        private Short postType;
        private Short uploadStatus;
        private Short publishType;
        private List<Long> images;
    }

    /**
     * 게시물 생성요청 DTO
     */
    @Data
    @Builder
    public static class Request01 {
        private String title;
        private String content;
        List<Long> images;
    }


    /**
     * 게시물 상세 조회 응답 DTO
     */
    @Data
    @Builder
    public static class Response01 {
        private Long id;
        private String title;
        private String content;
        private Short uploadStatus;
        private UserDTO.Response02 user;
        private List<FileResponse.Info> images;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    /**
     * 게시물 리스트 조회 응답 DTO
     */
    @Data
    @Builder
    public static class Response02 {
        private Long id;
        private String title;
        private String content;
        private Short uploadStatus;
        private UserDTO.Response02 user;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
