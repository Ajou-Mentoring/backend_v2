package pheonix.classconnect.backend.qna.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import pheonix.classconnect.backend.com.attachment.model.response.FileResponse;
import pheonix.classconnect.backend.com.auth.model.AuthorityDTO;
import pheonix.classconnect.backend.com.common.model.Paged;
import pheonix.classconnect.backend.com.common.model.Response;
import pheonix.classconnect.backend.com.user.model.UserDTO;
import pheonix.classconnect.backend.exceptions.ErrorCode;
import pheonix.classconnect.backend.exceptions.MainApplicationException;
import pheonix.classconnect.backend.qna.constants.PublishType;
import pheonix.classconnect.backend.qna.model.QnaDTO;
import pheonix.classconnect.backend.qna.service.QnaService;
import pheonix.classconnect.backend.security.service.PrincipalDetailsService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/v2")
@RequiredArgsConstructor
public class QnaController {
    private final QnaService qnaService;
    private final PrincipalDetailsService principalDetailsService;

    @GetMapping("/qna")
    public Response<Paged<QnaDTO.Response02>> getQnaPage(@RequestParam(value = "page", defaultValue = "0") int page,
                                                         @RequestParam(value = "size", defaultValue = "15") int size,
                                                         @RequestParam(value = "filterBy", required = false) String condition) {
        log.info("QnaController.getQnaPage()");

        Paged<QnaDTO.Response02> res;
        Paged<QnaDTO.Qna> qnaPage;
        if (condition == null || condition.isBlank()) {
            qnaPage = qnaService.getQnaPage(page, size, false);
        }
        else if (condition.equalsIgnoreCase("answered")) {
            qnaPage = qnaService.getQnaPage(page, size, true);
        }
        else {
            throw new MainApplicationException(ErrorCode.QNA_INVALID_PARAMETER, "지원하지 않는 조회 구분입니다.");
        }

        res = Paged.<QnaDTO.Response02>builder()
                .currentPage(qnaPage.getCurrentPage())
                .totalPages(qnaPage.getTotalPages())
                .numberOfElements(qnaPage.getNumberOfElements())
                .totalElements(qnaPage.getTotalElements())
                .size(qnaPage.getSize())
                .items(qnaPage.getItems().stream()
                        .map(qna -> QnaDTO.Response02.builder()
                                .id(qna.getId())
                                .title(qna.getTitle())
                                .answered(qna.getAnsweredAt() != null)
                                .isPublic(Objects.equals(qna.getPublishType(), PublishType.전체))
                                .user(UserDTO.Response02.builder()
                                        .id(qna.getQuestioner().getId())
                                        .email(qna.getQuestioner().getEmail())
                                        .department(qna.getQuestioner().getDepartment().getName())
                                        .name(qna.getQuestioner().getName())
                                        .studentNo(qna.getQuestioner().getStudentNo())
                                        .auth(Collections.max(qna.getQuestioner().getAuthorities().stream().map(AuthorityDTO.AuthorityInfo::getCode).toList()))
                                        .build())
                                .createdAt(qna.getCreatedAt())
                                .build())
                        .toList())
                .build();

        return Response.ok(HttpStatus.OK, "Q&A 게시물 페이지를 조회했습니다.", res);
    }

    @GetMapping("/qna/{id}")
    public Response<QnaDTO.Response01> getQna(@PathVariable(value = "id") Long qnaId,
                                              @AuthenticationPrincipal User user) {
        log.info("QnaController.getQna({})", qnaId);

        QnaDTO.Qna qna = qnaService.getQnaById(qnaId);

        // 비밀 질문은 본인 또는 관리자만 조회 가능
        if (qna.getPublishType() == PublishType.비밀) {
            if (user == null || (Long.parseLong(user.getUsername()) != qna.getQuestioner().getId() && !principalDetailsService.isAdmin(user))) {
                throw new MainApplicationException(ErrorCode.QNA_UNAUTHORIZED, "비밀 글은 본인 또는 관리자만 조회 가능합니다.");
            }
        }

        UserDTO.Response02 questioner = UserDTO.Response02.builder()
                .id(qna.getQuestioner().getId())
                .email(qna.getQuestioner().getEmail())
                .department(qna.getQuestioner().getDepartment().getName())
                .name(qna.getQuestioner().getName())
                .studentNo(qna.getQuestioner().getStudentNo())
                .auth(Collections.max(qna.getQuestioner().getAuthorities().stream().map(AuthorityDTO.AuthorityInfo::getCode).toList()))
                .build();

        UserDTO.Response02 answerer = qna.getAnswer() == null ? null : UserDTO.Response02.builder()
                .id(qna.getAnswerer().getId())
                .email(qna.getAnswerer().getEmail())
                .department(qna.getAnswerer().getDepartment().getName())
                .name(qna.getAnswerer().getName())
                .studentNo(qna.getAnswerer().getStudentNo())
                .auth(Collections.max(qna.getAnswerer().getAuthorities().stream().map(AuthorityDTO.AuthorityInfo::getCode).toList()))
                .build();

        QnaDTO.Response01 res = QnaDTO.Response01.builder()
                .question(QnaDTO.QuestionResponse01.builder()
                        .id(qna.getId())
                        .title(qna.getTitle())
                        .user(questioner)
                        .content(qna.getQuestion())
                        .images(qna.getQuestionImages().isEmpty() ? new ArrayList<>() : qna.getQuestionImages().stream()
                                .map(FileResponse.Info::fromFile)
                                .toList())
                        .isPublic(qna.getPublishType() == PublishType.전체)
                        .answered(qna.getAnsweredAt() != null)
                        .createdAt(qna.getCreatedAt())
                        .build())
                .answer(QnaDTO.AnswerResponse01.builder()
                        .content(qna.getAnswer())
                        .user(answerer)
                        .images(qna.getAnswerImages().isEmpty() ? new ArrayList<>() : qna.getAnswerImages().stream()
                                .map(FileResponse.Info::fromFile)
                                .toList())
                        .createdAt(qna.getAnsweredAt())
                        .build())
                .build();

        return Response.ok(HttpStatus.OK, "Q&A 게시물을 조회했습니다.", res);
    }

    @PostMapping("/qna")
    public Response<String> postQuestion(@RequestBody QnaDTO.Request01 req,
                                         @AuthenticationPrincipal User user) {
        log.info("QnaController.postQuestion()");

        // 요청 검증
        if (user == null) {
            throw new MainApplicationException(ErrorCode.BACK_INVALID_PERMISSION, "유저 권한 정보가 없습니다.");
        }
        QnaDTO.CreateQuestion dto = QnaDTO.CreateQuestion.builder()
                .title(req.getTitle())
                .content(req.getContent())
                .userId(Long.parseLong(user.getUsername()))
                .images(req.getImages())
                .publishType(req.getPublishType())
                .build();

        qnaService.createQuestion(dto);

        return Response.ok(HttpStatus.CREATED, "질문이 등록되었습니다.", null);
    }

    @PutMapping("/qna/{id}/question")
    public Response<String> updateQuestion(@PathVariable(name="id") Long qnaId,
                                           @RequestBody QnaDTO.Request02 req,
                                           @AuthenticationPrincipal User user) {
        log.info("QnaController.postQuestion({})", qnaId);

        // 요청 검증
        if (user == null) {
            throw new MainApplicationException(ErrorCode.BACK_INVALID_PERMISSION, "유저 권한 정보가 없습니다.");
        }

        // 본인이 올린 질문인지 검증
        QnaDTO.Qna qna = qnaService.getQnaById(qnaId);
        if (qna.getQuestioner().getId() != Long.parseLong(user.getUsername())) {
            throw new MainApplicationException(ErrorCode.QNA_UNAUTHORIZED, "질문자만 게시물을 수정할 수 있습니다.");
        }

        QnaDTO.UpdateQuestion dto = QnaDTO.UpdateQuestion.builder()
                .id(qnaId)
                .title(req.getTitle())
                .content(req.getContent())
                .images(req.getImages())
                .publishType(req.getPublishType())
                .build();

        qnaService.updateQuestion(dto);

        return Response.ok(HttpStatus.ACCEPTED, "질문이 수정되었습니다.", null);
    }

    @DeleteMapping ("/qna/{id}")
    public Response<String> deleteQuestion(@PathVariable(name="id") Long qnaId,
                                           @AuthenticationPrincipal User user) {
        log.info("QnaController.deleteQuestion({})", qnaId);

        // 요청 검증
        if (user == null) {
            throw new MainApplicationException(ErrorCode.BACK_INVALID_PERMISSION, "유저 권한 정보가 없습니다.");
        }

        // 본인이 올린 질문인지 검증
        QnaDTO.Qna qna = qnaService.getQnaById(qnaId);
        if (qna.getQuestioner().getId() != Long.parseLong(user.getUsername()) && !principalDetailsService.isAdmin(user)) {
            throw new MainApplicationException(ErrorCode.QNA_UNAUTHORIZED, "질문자만 게시물을 삭제할 수 있습니다.");
        }

        qnaService.removeQuestion(qnaId);

        return Response.ok(HttpStatus.ACCEPTED, "질문이 삭제되었습니다.", null);
    }

    @PostMapping("/qna/{id}/answer")
    public Response<String> postAnswer(@PathVariable(name="id") Long qnaId,
                                      @RequestBody QnaDTO.Request03 req,
                                      @AuthenticationPrincipal User user) {
        log.info("QnaController.postAnswer({})", qnaId);

        // 요청 검증
        // 관리자가 아니면 답변을 달 수 없음
        if (!principalDetailsService.isAdmin(user)) {
            throw new MainApplicationException(ErrorCode.BACK_INVALID_PERMISSION, "관리자만 답변을 달 수 있습니다.");
        }

        QnaDTO.Answer dto = QnaDTO.Answer.builder()
                .id(qnaId)
                .answer(req.getContent())
                .answerImages(req.getImages())
                .answererId(Long.parseLong(user.getUsername()))
                .build();

        qnaService.createAnswer(dto);
        
        return Response.ok(HttpStatus.CREATED, "답변이 등록되었습니다.", null);
    }

    @PutMapping("/qna/{id}/answer")
    public Response<String> updateAnswer(@PathVariable(name="id") Long qnaId,
                                       @RequestBody QnaDTO.Request03 req,
                                       @AuthenticationPrincipal User user) {
        log.info("QnaController.updateAnswer({})", qnaId);

        // 요청 검증
        // 관리자가 아니면 답변을 수정할 수 없음
        if (!principalDetailsService.isAdmin(user)) {
            throw new MainApplicationException(ErrorCode.BACK_INVALID_PERMISSION, "관리자만 답변을 수정할 수 있습니다.");
        }

        QnaDTO.Answer dto = QnaDTO.Answer.builder()
                .id(qnaId)
                .answer(req.getContent())
                .answerImages(req.getImages())
                .answererId(Long.parseLong(user.getUsername()))
                .build();

        qnaService.updateAnswer(dto);
        
        return Response.ok(HttpStatus.ACCEPTED, "답변이 수정되었습니다.", null);
    }

    @DeleteMapping("/qna/{id}/answer")
    public Response<String> deleteAnswer(@PathVariable(name="id") Long qnaId,
                                         @AuthenticationPrincipal User user) {
        log.info("QnaController.deleteAnswer({})", qnaId);

        // 요청 검증
        // 관리자가 아니면 답변을 삭제할 수 없음
        if (!principalDetailsService.isAdmin(user)) {
            throw new MainApplicationException(ErrorCode.BACK_INVALID_PERMISSION, "관리자만 답변을 삭제할 수 있습니다.");
        }

        qnaService.deleteAnswer(qnaId);

        return Response.ok(HttpStatus.ACCEPTED, "답변이 삭제되었습니다.", null);
    }
}
