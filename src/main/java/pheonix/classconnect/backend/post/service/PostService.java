package pheonix.classconnect.backend.post.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pheonix.classconnect.backend.com.attachment.constants.AttachmentDomainType;
import pheonix.classconnect.backend.com.attachment.model.File;
import pheonix.classconnect.backend.com.attachment.service.FileStorage;
import pheonix.classconnect.backend.com.common.model.Paged;
import pheonix.classconnect.backend.com.user.entity.UserEntity;
import pheonix.classconnect.backend.com.user.repository.UserRepository;
import pheonix.classconnect.backend.exceptions.ErrorCode;
import pheonix.classconnect.backend.exceptions.MainApplicationException;
import pheonix.classconnect.backend.post.constants.PostPublishType;
import pheonix.classconnect.backend.post.constants.PostType;
import pheonix.classconnect.backend.post.constants.PostUploadStatus;
import pheonix.classconnect.backend.post.entity.PostEntity;
import pheonix.classconnect.backend.post.model.PostDTO;
import pheonix.classconnect.backend.post.repository.PostRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FileStorage fileStorage;

    public void createPost(PostDTO.Create post, List<MultipartFile> images) {
        log.info("게시물 생성");

        // 입력값 검증
        if (!PostType.contains(post.getPostType())) {
            throw new MainApplicationException(ErrorCode.POST_INVALID_PARAMETER, String.format("지원하지 않는 게시물 구분입니다. [%d]", post.getPostType()));
        }
        if (!PostUploadStatus.contains(post.getUploadStatus())) {
            throw new MainApplicationException(ErrorCode.POST_INVALID_PARAMETER, String.format("지원하지 않는 등록 구분입니다. [%d]", post.getUploadStatus()));
        }
        if (!PostPublishType.contains(post.getPublishType())) {
            throw new MainApplicationException(ErrorCode.POST_INVALID_PARAMETER, String.format("지원하지 않는 게시 구분입니다. [%d]", post.getPublishType()));
        }

        // 본처리
        UserEntity writer = userRepository.findById(post.getWriterId())
                .orElseThrow(() -> new MainApplicationException(ErrorCode.USER_NOT_FOUND, "작성자 정보를 찾을 수 없습니다."));

        PostEntity newPost = PostEntity.builder()
                .id(null)
                .title(post.getTitle())
                .content(post.getContent())
                .postType(post.getPostType())
                .publishType(post.getPublishType())
                .uploadStatus(post.getUploadStatus())
                .writer(writer)
                .build();

        // 게시물 저장
        Long id = postRepository.save(newPost).getId();


        // 이미지 저장
        if (!images.isEmpty()) {
            images.forEach(image -> {
                fileStorage.saveFile(image, AttachmentDomainType.POST, id);
            });
        }
    }

    public PostDTO.Post getPost(Long id) {
        log.info("게시물 조회 [{}]", id);

        // 게시물 조회
        PostDTO.Post post = PostDTO.Post.fromEntity(postRepository.findById(id)
                .orElseThrow(() -> new MainApplicationException(ErrorCode.POST_NOT_FOUND, String.format("게시물을 찾을 수 없습니다. [%d]", id))));

        // 이미지 조회
        List<File> images = fileStorage.getAttachmentList(AttachmentDomainType.POST, id);

        post.setImages(images);

        return post;
    }

    public Paged<PostDTO.Post> getPostPage(Short postType, Short uploadStatus, Short publishType, int page, int size) {
        log.info("게시물 페이지 조회 [page: {}, size: {}]", page, size);

        // 입력값 검증
        if (!PostType.contains(postType)) {
            throw new MainApplicationException(ErrorCode.POST_INVALID_PARAMETER, String.format("지원하지 않는 게시물 구분입니다. [%d]", postType));
        }
        if (!PostUploadStatus.contains(uploadStatus)) {
            throw new MainApplicationException(ErrorCode.POST_INVALID_PARAMETER, String.format("지원하지 않는 등록 구분입니다. [%d]", uploadStatus));
        }
        if (!PostPublishType.contains(publishType)) {
            throw new MainApplicationException(ErrorCode.POST_INVALID_PARAMETER, String.format("지원하지 않는 게시 구분입니다. [%d]", publishType));
        }

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(List.of(
                        Sort.Order.desc("createdDate"),
                        Sort.Order.desc("createdTime"))
                )
        );

        Page<PostEntity> posts = postRepository.findAllByPostTypeLessThanEqualAndUploadStatusLessThanAndPublishTypeLessThanEqual(postType, uploadStatus, publishType, pageable);

        return Paged.<PostDTO.Post>builder()
                .currentPage(posts.getNumber())
                .size(posts.getSize())
                .numberOfElements(posts.getNumberOfElements())
                .totalPages(posts.getTotalPages())
                .totalElements(posts.getTotalElements())
                .items(posts.getContent().stream().map(PostDTO.Post::fromEntity).toList())
                .build();
    }

    public void updatePost(Long id, PostDTO.Update updated) {
        log.info("게시물 수정 [{}]", id);

        // 입력값 검증
        PostEntity post = postRepository.findById(id)
                .orElseThrow(() -> new MainApplicationException(ErrorCode.POST_NOT_FOUND, String.format("게시물을 찾을 수 없습니다. [%d]", id)));

        if (!PostType.contains(updated.getPostType())) {
            throw new MainApplicationException(ErrorCode.POST_INVALID_PARAMETER, String.format("지원하지 않는 게시물 구분입니다. [%d]", post.getPostType()));
        }
        if (!PostUploadStatus.contains(updated.getUploadStatus())) {
            throw new MainApplicationException(ErrorCode.POST_INVALID_PARAMETER, String.format("지원하지 않는 등록 구분입니다. [%d]", post.getUploadStatus()));
        }
        if (!PostPublishType.contains(updated.getPublishType())) {
            throw new MainApplicationException(ErrorCode.POST_INVALID_PARAMETER, String.format("지원하지 않는 게시 구분입니다. [%d]", post.getPublishType()));
        }

        post.updatePost(updated.getPostType(), updated.getUploadStatus(), updated.getPublishType(), updated.getTitle(), updated.getContent());

        postRepository.save(post);
    }

    public void deletePost(Long id) {
        log.info("게시물 삭제 [{}]", id);
        // 입력값 검증
        // 입력값 검증
        PostEntity post = postRepository.findById(id)
                .orElseThrow(() -> new MainApplicationException(ErrorCode.POST_NOT_FOUND, String.format("게시물을 찾을 수 없습니다. [%d]", id)));

        // 게시물 삭제
        postRepository.delete(post);

        // 이미지 삭제
        fileStorage.deleteAllFilesIn(AttachmentDomainType.POST, id);
    }
}
