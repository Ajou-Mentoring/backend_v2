package pheonix.classconnect.backend.qna.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pheonix.classconnect.backend.com.attachment.service.FileStorage;
import pheonix.classconnect.backend.com.user.repository.UserRepository;
import pheonix.classconnect.backend.com.attachment.constants.AttachmentDomainType;
import pheonix.classconnect.backend.com.attachment.model.File;
import pheonix.classconnect.backend.com.user.entity.UserEntity;
import pheonix.classconnect.backend.exceptions.ErrorCode;
import pheonix.classconnect.backend.exceptions.MainApplicationException;
import pheonix.classconnect.backend.qna.entity.QnaEntity;
import pheonix.classconnect.backend.qna.model.QnaDTO;
import pheonix.classconnect.backend.qna.repository.QnaRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class QnaService {
    private final QnaRepository qnaRepository;
    private final UserRepository userRepository;
    private final FileStorage fileStorage;

    // 질문 생성 - 이미지 포함
    public void createQna(QnaDTO.CreateQuestion dto) {
        log.info("질문 생성");

        // 질문자 유저 정보 조회
        UserEntity questioner = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new MainApplicationException(ErrorCode.USER_NOT_FOUND, "질문을 등록할 유저 정보를 찾을 수 없습니다."));

        // 질문 등록
        QnaEntity saved = QnaEntity.builder()
                .id(null)
                .title(dto.getTitle())
                .question(dto.getContent())
                .questioner(questioner)
                .publishType(dto.getPublishType())
                .build();

        Long qnaId = qnaRepository.save(saved).getId();

        // 추가된 이미지 저장
        if (!dto.getImages().isEmpty()) {
            dto.getImages().forEach(imgId -> {
                File image = fileStorage.getFileById(imgId);
                fileStorage.mapFileToDomain(image.getId(), AttachmentDomainType.QUESTION, qnaId);
            });
        }
    }

    // 질문 수정 - 단 답변 달릴 경우 수정 불가
    public void updateQuestion(QnaDTO.UpdateQuestion dto) {
        // 입력값 검증
        QnaEntity entity = qnaRepository.findById(dto.getId())
                .orElseThrow(() -> new MainApplicationException(ErrorCode.QNA_NOT_FOUND, String.format("수정할 Q&A를 찾을 수 없습니다. [%d]", dto.getId())));
        if (!entity.getId().equals(dto.getUserId())) {
            throw new MainApplicationException(ErrorCode.QNA_UNAUTHORIZED, "등록자와 수정 요청자 정보가 다릅니다.");
        }

        // 본처리

    }

    // 질문 삭제 - 이미지 삭제

    // 질문 조회 - 이미지 포함, 게시구분 조건, 답변 포함

    // 질문 리스트 조회

    // 답변 생성 - 이미지 포함, 삭제가 아니면

    // 답변 수정 - 이미지 포함, 삭제가 아니면
}
