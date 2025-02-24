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
import pheonix.classconnect.backend.security.model.UserDetailDTO;
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
                                .updated(qna.getUpdatedAt() != null && qna.getAnsweredAt() == null)
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
            if (user == null || (Long.parseLong(user.getUsername()) != qna.getQuestioner().getId() || principalDetailsService.isAdmin(user))) {
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
                        .updated(qna.getUpdatedAt() == qna.getCreatedAt())
                        .build())
                .answer(QnaDTO.AnswerResponse01.builder()
                        .content(qna.getAnswer())
                        .user(answerer)
                        .images(qna.getAnswerImages().isEmpty() ? new ArrayList<>() : qna.getAnswerImages().stream()
                                .map(FileResponse.Info::fromFile)
                                .toList())
                        .createdAt(qna.getAnsweredAt())
                        .updated(qna.getUpdatedAt() != qna.getAnsweredAt())
                        .build())
                .build();

        return Response.ok(HttpStatus.OK, "Q&A 게시물을 조회했습니다.", res);
    }

    @PostMapping("/qna")
    public Response<String> postQuestion(@RequestBody QnaDTO.Request01 req,
                                         @AuthenticationPrincipal User user) {
        log.info("QnaController.postQuestion()");

        return Response.ok(HttpStatus.CREATED, "질문이 등록되었습니다.", null);
    }

    @PutMapping("/qna/{id}/question")
    public Response<String> updateQuestion(@PathVariable(name="id") Long qnaId,
                                           @RequestBody QnaDTO.Response02 req,
                                           @AuthenticationPrincipal User user) {
        log.info("QnaController.postQuestion({})", qnaId);

        return Response.ok(HttpStatus.ACCEPTED, "질문이 수정되었습니다.", null);
    }

    @DeleteMapping ("/qna/{id}")
    public Response<String> deleteQuestion(@PathVariable(name="id") Long qnaId,
                                           @AuthenticationPrincipal User user) {
        log.info("QnaController.deleteQuestion({})", qnaId);

        return Response.ok(HttpStatus.ACCEPTED, "질문이 삭제되었습니다.", null);
    }

    @PostMapping("/qna/{id}/answer")
    public Response<String> putAnswer(@PathVariable(name="id") Long qnaId,
                                      @RequestBody QnaDTO.Request01 req,
                                      @AuthenticationPrincipal User user) {
        log.info("QnaController.addAnswer({})", qnaId);
        int action = 1; // 동작: 1-답변등록 2-답변수정


        return switch (action) {
            case 1 -> {
                log.info("답변등록");
                yield Response.ok(HttpStatus.CREATED, "답변이 등록되었습니다.", null); // 답변 등록
            }
            case 2 -> {
                log.info("답변수정");
                yield Response.ok(HttpStatus.ACCEPTED, "답변이 수정되었습니다.", null);
            }
            default -> Response.error(ErrorCode.BAK_LOGIC_ERROR, "답변을 등록/수정할 수 없는 게시물입니다.");
        };

    }
}
