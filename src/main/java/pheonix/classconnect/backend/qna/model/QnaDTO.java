package pheonix.classconnect.backend.qna.model;

import lombok.Builder;
import lombok.Data;
import pheonix.classconnect.backend.com.attachment.model.File;
import pheonix.classconnect.backend.com.attachment.model.response.FileResponse;
import pheonix.classconnect.backend.com.user.model.UserDTO;
import pheonix.classconnect.backend.qna.entity.QnaEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class QnaDTO {
    @Data
    @Builder
    public static class Qna {
        private Long id;
        private String title;
        private Short publishType;
        private String question;
        private List<File> questionImages;
        private UserDTO.User questioner;
        private String answer;
        private List<File> answerImages;
        private UserDTO.User answerer;
        private LocalDate answerDate;
        private LocalTime answerTime;
        private Boolean answered;

        public static Qna fromEntity(QnaEntity entity) {
            return Qna.builder()
                    .id(entity.getId())
                    .publishType(entity.getPublishType())
                    .title(entity.getTitle())
                    .question(entity.getQuestion())
                    .questionImages(new ArrayList<>())
                    .questioner(UserDTO.User.fromEntity(entity.getQuestioner()))
                    .answer(entity.getAnswer())
                    .answerImages(new ArrayList<>())
                    .answerer(UserDTO.User.fromEntity(entity.getAnswerer()))
                    .answerDate(entity.getAnswerDate())
                    .answerTime(entity.getAnswerTime())
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

    @Data
    @Builder
    public static class Request01 {
        private String title;
        private Short publishType;
        private String question;
        private List<Long> questionImages;
        private Long questionerId;
    }

    @Data
    @Builder
    public static class Request02 {
        private String answer;
        private List<Long> answerImages;
        private Long answererId;
    }

    // 상세조회
    @Data
    @Builder
    public static class Response01 {
        private Long id;
        private String title;
        private Short publishType;
        private String question;
        private List<FileResponse.Info> questionImages;
        private UserDTO.Response02 questioner;
        private String answer;
        private List<FileResponse.Info> answerImages;
        private UserDTO.Response02 answerer;
        private LocalDateTime answeredAt;
        private Boolean answered;
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
}
