package pheonix.classconnect.backend.qna.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pheonix.classconnect.backend.com.attachment.service.FileStorage;
import pheonix.classconnect.backend.com.common.model.Paged;
import pheonix.classconnect.backend.com.user.repository.UserRepository;
import pheonix.classconnect.backend.com.attachment.constants.AttachmentDomainType;
import pheonix.classconnect.backend.com.user.entity.UserEntity;
import pheonix.classconnect.backend.exceptions.ErrorCode;
import pheonix.classconnect.backend.exceptions.MainApplicationException;
import pheonix.classconnect.backend.qna.entity.QnaEntity;
import pheonix.classconnect.backend.qna.model.QnaDTO;
import pheonix.classconnect.backend.qna.repository.QnaRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QnaService {
    private final QnaRepository qnaRepository;
    private final UserRepository userRepository;
    private final FileStorage fileStorage;

    // 질문 생성 - 이미지 포함
    @Transactional
    public void createQuestion(QnaDTO.CreateQuestion dto) {
        log.info("질문 생성");

        // 질문자 유저 정보 조회
        UserEntity questioner = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new MainApplicationException(ErrorCode.USER_NOT_FOUND, "유저 정보를 찾을 수 없습니다."));

        // 질문 등록
        QnaEntity saved = QnaEntity.builder()
                .id(null)
                .title(dto.getTitle())
                .question(dto.getContent())
                .questioner(questioner)
                .publishType(dto.getPublishType())
                .answered(false)
                .build();

        Long qnaId = qnaRepository.save(saved).getId();

        // 추가된 이미지 저장
        if (!dto.getImages().isEmpty()) {
            dto.getImages().forEach(imgId -> {
                fileStorage.mapFileToDomain(imgId, AttachmentDomainType.QUESTION, qnaId);
            });
        }

    }

    // 질문 수정 - 단 답변 달릴 경우 수정 불가
    public void updateQuestion(QnaDTO.UpdateQuestion dto) {
        // 입력값 검증
        QnaEntity entity = qnaRepository.findById(dto.getId())
                .orElseThrow(() -> new MainApplicationException(ErrorCode.QNA_NOT_FOUND, String.format("수정할 Q&A를 찾을 수 없습니다. [%d]", dto.getId())));
        if (entity.getAnswered()) {
            throw new MainApplicationException(ErrorCode.QNA_BAD_REQUEST, "답변이 달린 게시물은 수정할 수 없습니다.");
        }
        // 본처리
        // 질문 생성
        entity.updateQuestion(dto.getTitle(), dto.getContent(), dto.getPublishType());

        qnaRepository.save(entity);

        // 이미지 수정
        fileStorage.changeFiles(AttachmentDomainType.QUESTION, entity.getId(), dto.getImages());
    }

    // 질문 삭제
    @Transactional
    public void removeQuestion(Long id) {
        log.info("질문 삭제");

        // 요청 검증
        // 입력값 검증
        QnaEntity entity = qnaRepository.findById(id)
                .orElseThrow(() -> new MainApplicationException(ErrorCode.QNA_NOT_FOUND, String.format("삭제할 Q&A를 찾을 수 없습니다. [%d]", id)));

        // 본처리
        fileStorage.deleteAllFilesIn(AttachmentDomainType.QUESTION, id);

        qnaRepository.delete(entity);
    }

    // 질문 조회 - 이미지 포함, 게시구분 조건, 답변 포함
    public QnaDTO.Qna getQnaById(Long id) {
        log.info("Q&A 조회");

        // 요청 검증
        // 입력값 검증
        QnaEntity entity = qnaRepository.findById(id)
                .orElseThrow(() -> new MainApplicationException(ErrorCode.QNA_NOT_FOUND, String.format("조회할 Q&A를 찾을 수 없습니다. [%d]", id)));

        // 본처리
        QnaDTO.Qna qna = QnaDTO.Qna.fromEntity(entity);
        // 이미지
        qna.setQuestionImages(fileStorage.getAttachmentList(AttachmentDomainType.QUESTION, entity.getId()));
        qna.setAnswerImages(fileStorage.getAttachmentList(AttachmentDomainType.ANSWER, entity.getId()));

        return qna;
    }

    public QnaDTO.Qna getQnaAndUsersByQnaId(Long id){
        log.info("Q&A 조회");

        // 요청 검증
        QnaEntity entity = qnaRepository.findByIdWithUsers(id)
                .orElseThrow(() -> new MainApplicationException(ErrorCode.QNA_NOT_FOUND, String.format("조회할 Q&A를 찾을 수 없습니다. [%d]", id)));

        // 본처리
        QnaDTO.Qna qna = QnaDTO.Qna.fromEntity(entity);

        return qna;
    }

    // 질문 리스트 조회
    public Paged<QnaDTO.Qna> getQnaPage(int page, int size, boolean notAnsweredOnly) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(List.of(
                        Sort.Order.desc("createdDate"),
                        Sort.Order.desc("createdTime")
                )
        ));

        Page<QnaEntity> qnaList;
        if (notAnsweredOnly) {
            qnaList = qnaRepository.findAllByAnsweredFalse(pageable);
        }
        else {
            qnaList = qnaRepository.findAll(pageable);
        }

        return Paged.<QnaDTO.Qna>builder()
                .currentPage(qnaList.getNumber())
                .size(qnaList.getSize())
                .numberOfElements(qnaList.getNumberOfElements())
                .totalPages(qnaList.getTotalPages())
                .totalElements(qnaList.getTotalElements())
                .items(qnaList.getContent().stream()
                        .map(QnaDTO.Qna::fromEntity)
                        .toList())
                .build();
    }

    @Transactional
    // 답변 생성 - 이미지 포함, 삭제가 아니면
    public void createAnswer(QnaDTO.Answer dto) {
        log.info("답변 생성");

        QnaEntity qna = qnaRepository.findById(dto.getId())
                .orElseThrow(() -> new MainApplicationException(ErrorCode.QNA_NOT_FOUND,
                        String.format("조회할 Q&A를 찾을 수 없습니다. [%d]", dto.getId())));

        UserEntity answerer = userRepository.findById(dto.getAnswererId())
                .orElseThrow(() -> new MainApplicationException(ErrorCode.USER_NOT_FOUND, "답변자 정보를 찾을 수 없습니다."));

        qna.addAnswer(dto.getAnswer(), answerer);

        qnaRepository.save(qna);

        // 이미지 매핑
        if (!dto.getAnswerImages().isEmpty()) {
            for (Long imgId : dto.getAnswerImages()) {
                fileStorage.mapFileToDomain(imgId, AttachmentDomainType.ANSWER, qna.getId());
            }
        }
    }

    // 답변 수정 - 이미지 포함, 삭제가 아니면
    @Transactional
    public void updateAnswer(QnaDTO.Answer dto) {
        log.info("답변 수정");

        // 입력값 검증
        QnaEntity qna = qnaRepository.findById(dto.getId())
                .orElseThrow(() -> new MainApplicationException(ErrorCode.QNA_NOT_FOUND, "Q&A 게시물을 찾을 수 없습니다."));

        UserEntity answerer = userRepository.findById(dto.getAnswererId())
                .orElseThrow(() -> new MainApplicationException(ErrorCode.USER_NOT_FOUND, "답변자 정보를 찾을 수 없습니다."));

        if (!qna.getAnswered()) {
            throw new MainApplicationException(ErrorCode.QNA_BAD_REQUEST, "답변이 완료된 게시물의 답변만 수정 가능합니다.");
        }

        qna.addAnswer(dto.getAnswer(), answerer);

        qnaRepository.save(qna);

        // 이미지 교체
        fileStorage.changeImages(AttachmentDomainType.ANSWER, qna.getId(), dto.getAnswerImages());
    }

    // 답변 삭제
    @Transactional
    public void deleteAnswer(Long id) {
        log.info("답변 삭제");

        // 입력값 검증
        QnaEntity qna = qnaRepository.findById(id)
                .orElseThrow(() -> new MainApplicationException(ErrorCode.QNA_NOT_FOUND, "Q&A 게시물을 찾을 수 없습니다."));

        qna.removeAnswer();

        qnaRepository.save(qna);

        // 이미지 삭제
        fileStorage.deleteAllFilesIn(AttachmentDomainType.ANSWER, id);
    }
}
