package pheonix.classconnect.backend.qna.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import pheonix.classconnect.backend.com.common.model.Paged;
import pheonix.classconnect.backend.com.common.model.Response;
import pheonix.classconnect.backend.exceptions.ErrorCode;
import pheonix.classconnect.backend.qna.model.QnaDTO;
import pheonix.classconnect.backend.qna.service.QnaService;

@Slf4j
@RestController
@RequestMapping("/v2")
@RequiredArgsConstructor
public class QnaController {
    private final QnaService qnaService;

    @GetMapping("/qna")
    public Response<Paged<QnaDTO.Response02>> getQnaPage(@RequestParam(value = "page", defaultValue = "0") int page,
                                                         @RequestParam(value = "size", defaultValue = "15") int size) {
        log.info("QnaController.getQnaPage()");

        return null;
    }

    @GetMapping("/qna/{id}")
    public Response<QnaDTO.Response01> getQna(@PathVariable(value = "id") Long qnaId,
                                              @AuthenticationPrincipal User user) {
        log.info("QnaController.getQna({})", qnaId);

        return null;
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

    @PutMapping("/qna/{id}/answer")
    public Response<String> putAnswer(@PathVariable(name="id") Long qnaId,
                                      @RequestBody QnaDTO.Response02 req,
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
