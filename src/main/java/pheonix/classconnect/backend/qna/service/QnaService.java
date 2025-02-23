package pheonix.classconnect.backend.qna.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pheonix.classconnect.backend.com.attachment.service.FileStorage;
import pheonix.classconnect.backend.com.user.repository.UserRepository;
import pheonix.classconnect.backend.qna.repository.QnaRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class QnaService {
    private final QnaRepository qnaRepository;
    private final UserRepository userRepository;
    private final FileStorage fileStorage;

    // 질문 생성 - 이미지 포함

    // 질문 수정 - 단 답변 달릴 경우 수정 불가

    // 질문 삭제 - 이미지 삭제

    // 질문 조회 - 이미지 포함, 게시구분 조건, 답변 포함

    // 질문 리스트 조회

    // 답변 생성 - 이미지 포함, 삭제가 아니면

    // 답변 수정 - 이미지 포함, 삭제가 아니면
}
