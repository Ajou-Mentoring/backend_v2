package pheonix.classconnect.backend.qna.model;


import lombok.Builder;
import lombok.Data;
import pheonix.classconnect.backend.com.attachment.model.response.FileResponse;
import pheonix.classconnect.backend.com.user.model.UserDTO;
import pheonix.classconnect.backend.qna.entity.QnaEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class QnaDTO {

    // 질문 상세 응답
    @Data
    @Builder
    public static class Qna {
        private Long id;                            /*게시물 ID*/
        private String title;                       /*제목*/
        private String question;                    /*질문*/
        private List<Long> questionImages;          /*질문 이미지 리스트*/
        private Short publishType;                  /*공개여부*/
        private UserDTO.User questioner;            /*질문자*/
        private LocalDateTime createdAt;            /*등록일*/
        private LocalDateTime updatedAt;            /*등록시간*/
        private boolean answered;                   /*답변여부*/
        private String answer;                      /*답변*/
        private List<Long> answerImages;            /*질문 이미지 리스트*/
        private UserDTO.User answerer;              /*답변자*/
        private LocalDateTime answeredAt;           /*답변시간*/

        public static QnaDTO.Qna fromEntity(QnaEntity entity) {
            return Qna.builder()
                    .id(entity.getId())
                    .publishType(entity.getPublishType())
                    .title(entity.getTitle())
                    .question(entity.getQuestion())
                    .questionImages(new ArrayList<>())
                    .questioner(UserDTO.User.fromEntity(entity.getQuestioner()))
                    .createdAt(LocalDateTime.of(entity.getCreatedDate(), entity.getCreatedTime()))
                    .updatedAt(LocalDateTime.of(entity.getUpdatedDate(), entity.getUpdatedTime()))
                    .answer(entity.getAnswer())
                    .answerImages(new ArrayList<>())
                    .answerer(UserDTO.User.fromEntity(entity.getAnswerer()))
                    .answeredAt(LocalDateTime.of(entity.getAnswerDate(), entity.getAnswerTime()))
                    .answered(entity.getAnswered())
                    .build();
        }
    }

    @Data
    @Builder
    public static class Question {
        private Short publishType;
        private String title;
        private String question;
        private List<Long> questionImages;
        private Long questionerId;
    }

    @Data
    @Builder
    public static class Answer {
        private String answer;
        private List<Long> answerImages;
        private Long answererId;
    }

    // 질문 생성 요청
    @Data
    @Builder
    public static class Request01 {
        private String title;       /*제목*/
        private String question;    /*질문*/
        private Short PublishType;  /*공개여부*/
        private List<Long> images;  /*이미지 리스트*/
    }

    // 질문 수정 요청
    @Data
    @Builder
    public static class Request02 {
        private String title;       /*제목*/
        private String question;    /*질문*/
        private Short publishType;  /*공개여부*/
        private List<Long> images;  /*이미지 리스트*/
    }

    // 질문 생성 DTO
    @Data
    @Builder
    public static class CreateQuestion {
        private Long userId;        /*질문자 ID*/
        private String title;       /*제목*/
        private String content;     /*질문*/
        private Short publishType;  /*공개여부*/
        private List<Long> images;  /*이미지 리스트*/
    }

    // 질문 수정 DTO
    @Data
    @Builder
    public static class UpdateQuestion {
        private Long id;            /*Q&A ID*/
        private Long userId;        /*질문자 ID*/
        private String title;       /*제목*/
        private String question;    /*질문*/
        private Short PublishType;  /*공개여부*/
        private List<Long> images;  /*이미지 리스트*/
    }

    @Data
    @Builder
    public static class QuestionResponse01 {
        private UserDTO.Response02 user;
        private String content;
        private List<FileResponse> images;
        private LocalDateTime createdAt;
        private boolean updated;
    }

    // 리스트
    @Data
    @Builder
    public static class Response11 {
        private Long id;
        private String title;
        private Short publishType;
        private String question;
        private UserDTO.Response02 questioner;
        private Boolean answered;
    }

    public static class AnswerResponse01 {
        private UserDTO.Response02 user;
        private String content;
        private List<FileResponse> images;
        private LocalDateTime createdAt;
        private boolean updated;
    }

    // 질문 상세 응답
    @Data
    @Builder
    public static class Response01 {
        private Long id;                            /*게시물 ID*/
        private String title;                       /*제목*/
        private Short isPublic;                     /*공개여부*/
        private boolean answered;                   /*답변 여부*/
        private QuestionResponse01 question;        /*질문*/
        private AnswerResponse01 answer;            /*답변*/
    }

    // 질문 리스트 응답
    @Data
    @Builder
    public static class Response02 {
        private Long id;                            /*게시물 ID*/
        private String title;                       /*제목*/
        private UserDTO.Response02 user;            /*질문자*/
        private Short isPublic;                     /*공개여부*/
        private boolean answered;                   /*답변 여부*/
        private boolean updated;                    /*수정 여부*/
        private LocalDateTime createdAt;            /*질문 등록일*/
    }
}
