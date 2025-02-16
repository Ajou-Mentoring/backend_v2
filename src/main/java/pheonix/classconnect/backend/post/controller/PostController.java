package pheonix.classconnect.backend.post.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import pheonix.classconnect.backend.com.attachment.constants.AttachmentDomainType;
import pheonix.classconnect.backend.com.attachment.model.response.FileResponse;
import pheonix.classconnect.backend.com.attachment.service.FileStorage;
import pheonix.classconnect.backend.com.auth.model.AuthorityDTO;
import pheonix.classconnect.backend.com.common.model.Paged;
import pheonix.classconnect.backend.com.common.model.Response;
import pheonix.classconnect.backend.com.user.model.UserDTO;
import pheonix.classconnect.backend.exceptions.ErrorCode;
import pheonix.classconnect.backend.exceptions.MainApplicationException;
import pheonix.classconnect.backend.post.constants.PostPublishType;
import pheonix.classconnect.backend.post.constants.PostType;
import pheonix.classconnect.backend.post.constants.PostUploadStatus;
import pheonix.classconnect.backend.post.model.PostDTO;
import pheonix.classconnect.backend.post.service.PostService;
import pheonix.classconnect.backend.security.service.PrincipalDetailsService;

import java.util.Collections;

@Slf4j
@RestController
@RequestMapping("/v2")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final FileStorage fileStorage;
    private final PrincipalDetailsService principalDetailsService;

    @PostMapping(value = "/notice", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Response<String> postNotice(@ModelAttribute PostDTO.Request01 request,
                                       @AuthenticationPrincipal User user) {
        log.info("PostController.postNotice()");

        // 요청 검증
        if (!principalDetailsService.isAdmin(user)) {
            throw new MainApplicationException(ErrorCode.BACK_INVALID_PERMISSION, "관리자만 공지사항을 게시할 수 있습니다.");
        }

        // 본처리
        PostDTO.Create dto = PostDTO.Create.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .postType(PostType.공지사항)
                .uploadStatus(PostUploadStatus.게시)
                .publishType(PostPublishType.전체)
                .writerId(Long.parseLong(user.getUsername()))
                .build();

        postService.createPost(dto, request.getImages());

        return Response.ok(HttpStatus.CREATED, "공지사항이 등록되었습니다.", null);
    }

    @PutMapping(value = "/notice/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Response<String> updateNotice(@PathVariable Long id,
                                       @ModelAttribute PostDTO.Request01 req,
                                       @AuthenticationPrincipal User user) {
        log.info("PostController.updateNotice()");

        // 요청 검증
        if (!principalDetailsService.isAdmin(user)) {
            throw new MainApplicationException(ErrorCode.BACK_INVALID_PERMISSION, "관리자만 공지사항을 수정할 수 있습니다.");
        }

        // 본처리
        PostDTO.Update dto = PostDTO.Update.builder()
                .title(req.getTitle())
                .content(req.getContent())
                .postType(PostType.공지사항)
                .uploadStatus(PostUploadStatus.수정)
                .publishType(PostPublishType.전체)
                .build();

        postService.updatePost(id, dto);

        // 기존 이미지 제거
        fileStorage.deleteAllFilesIn(AttachmentDomainType.POST, id);

        // 새 이미지 추가
        if (!req.getImages().isEmpty()) {
            req.getImages().forEach(image -> {
                fileStorage.saveFile(image, AttachmentDomainType.POST, id);
            });
        }

        return Response.ok(HttpStatus.CREATED, "공지사항이 수정되었습니다.", null);
    }

    @DeleteMapping("/notice/{id}")
    public Response<String> removeNotice(@PathVariable Long id,
                                       @AuthenticationPrincipal User user) {

        log.info("PostController.removeNotice()");

        // 요청 검증
        if (!principalDetailsService.isAdmin(user)) {
            throw new MainApplicationException(ErrorCode.BACK_INVALID_PERMISSION, "관리자만 공지사항을 삭제할 수 있습니다.");
        }

        // 본처리
        // 공지사항 삭제
        postService.deletePost(id);

        return Response.ok(HttpStatus.OK, "공지사항을 삭제했습니다.", null);
    }

    @GetMapping("/notice/{id}")
    public Response<PostDTO.Response01> getNotice(@PathVariable Long id) {
        log.info("PostController.getNotice()");

        // 요청 검증 없음

        // 본처리
        PostDTO.Post post = postService.getPost(id);

        // 출력 조립
        PostDTO.Response01 res = PostDTO.Response01.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .uploadStatus(post.getUploadStatus())
                .images(post.getImages().stream().map(FileResponse.Info::fromFile).toList())
                .user(UserDTO.Response02.builder()
                        .id(post.getWriter().getId())
                        .name(post.getWriter().getName())
                        .email(post.getWriter().getEmail())
                        .studentNo(post.getWriter().getStudentNo())
                        .auth(Collections.max(post.getWriter().getAuthorities().stream().map(AuthorityDTO.AuthorityInfo::getCode).toList()))
                        .build())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();

        return Response.ok(HttpStatus.OK, "공지사항을 조회했습니다.", res);
    }

    @GetMapping("/notice")
    public Response<Paged<PostDTO.Response02>> getNoticePage(@RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "15") int size,
                                                             @AuthenticationPrincipal User user) {
        log.info("PostController.getNoticePage()");

        // 요청 검증
        Short allowedStatus;
        Short allowedPublishType;

        if (principalDetailsService.isAdmin(user)) {
            allowedStatus = PostUploadStatus.삭제;
            allowedPublishType = PostPublishType.관리자;
        } else {
            allowedStatus = PostUploadStatus.수정;
            allowedPublishType = PostPublishType.전체;
        }

        // 본처리
        Paged<PostDTO.Post> posts = postService.getPostPage(PostType.공지사항, allowedStatus, allowedPublishType, page, size);
        Paged<PostDTO.Response02> res = Paged.<PostDTO.Response02>builder()
                .currentPage(posts.getCurrentPage())
                .size(posts.getSize())
                .numberOfElements(posts.getNumberOfElements())
                .totalPages(posts.getTotalPages())
                .totalElements(posts.getTotalElements())
                .items(posts.getItems().stream().map(post -> PostDTO.Response02.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .content(post.getContent())
                        .uploadStatus(post.getUploadStatus())
                        .createdAt(post.getCreatedAt())
                        .updatedAt(post.getUpdatedAt())
                        .user(UserDTO.Response02.builder()
                                .id(post.getWriter().getId())
                                .name(post.getWriter().getName())
                                .email(post.getWriter().getEmail())
                                .studentNo(post.getWriter().getStudentNo())
                                .auth(Collections.max(post.getWriter().getAuthorities().stream().map(AuthorityDTO.AuthorityInfo::getCode).toList()))
                                .build())
                        .build()).toList())
                .build();

        return Response.ok(HttpStatus.OK, "공지사항 페이지를 조회했습니다.", res);
    }

}
