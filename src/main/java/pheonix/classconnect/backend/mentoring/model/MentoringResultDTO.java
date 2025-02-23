package pheonix.classconnect.backend.mentoring.model;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import pheonix.classconnect.backend.com.attachment.model.File;
import pheonix.classconnect.backend.com.attachment.model.response.FileResponse;
import pheonix.classconnect.backend.com.user.model.UserDTO;
import pheonix.classconnect.backend.course.model.CourseDTO;
import pheonix.classconnect.backend.mentoring.entity.MentoringRequestEntity;
import pheonix.classconnect.backend.mentoring.entity.MentoringResultEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MentoringResultDTO {
    @Data
    @Builder
    public static class MentoringResult {
        private Long id;
        private LocalDate date;
        private LocalTime time;
        private Integer length;
        private String location;
        private String content;
        private Map<String, Object> mentees;
        private UserDTO.User mentor;
        private CourseDTO.Course course;
        private MentoringRequestDTO.MentoringRequest request;
        private List<File> images;

        public static MentoringResult fromEntity(MentoringResultEntity entity) {
            return MentoringResult.builder()
                    .id(entity.getId())
                    .date(entity.getDate())
                    .time(entity.getTime())
                    .length(entity.getLength())
                    .location(entity.getLocation())
                    .content(entity.getContent())
                    .mentees(entity.getMentees())
                    .mentor(entity.getMentor() == null ? null : UserDTO.User.fromEntity(entity.getMentor()))
                    .course(entity.getCourse() == null ? null : CourseDTO.Course.fromEntity(entity.getCourse()))
                    .request(entity.getRequest() == null ? null : MentoringRequestDTO.MentoringRequest.fromEntity(entity.getRequest()))
                    .images(new ArrayList<>())
                    .build();
        }
    }

    @Data
    @Builder
    public static class Mentee {
        private String name;
        private String studentNo;
    }


    @Data
    @Builder
    public static class Create {
        private LocalDate date;
        private LocalTime time;
        private Integer length;
        private String location;
        private String content;
        private Map<String, Object> mentees;
        private Long mentorId;
        private Long courseId;
        private Long requestId;
        private List<Long> images;

        public void addMentee(String name, String studentId) {
            if (this.mentees.isEmpty()) {
                this.mentees = new HashMap<>();
            }

            mentees.put(studentId, name);
        }

    }

    @Data
    @Builder
    public static class Update {
        private Long id;
        private LocalDate date;
        private LocalTime time;
        private Integer length;
        private String location;
        private String content;
        private Map<String, Object> mentees;
        private List<Long> images;
        private int action;

        public void addMentee(String name, String studentId) {
            if (this.mentees.isEmpty()) {
                this.mentees = new HashMap<>();
            }

            mentees.put(studentId, name);
        }
    }

    // 생성
    @Data
    @Builder
    public static class Request01 {
        @NotNull(message = "멘토링 일자 값이 비어있습니다.")
        private LocalDate date;
        @NotNull(message = "멘토링 시작 시간 값이 비어있습니다.")
        private LocalTime time;
        @NotNull(message = "멘토링 진행 시간 값이 비어있습니다.")
        private Integer duration;
        @NotNull(message = "멘토링 진행 장소 값이 비어있습니다.")
        private String location;
        @NotNull(message = "멘토링 진행 내용이 비어있습니다.")
        private String content;
        @NotNull(message = "멘토링 대상 멘티 목록이 NULL 입니다.")
        private List<Mentee> mentees;
        @NotNull(message = "증빙자료 이미지 목록이 NULL 입니다.")
        private List<Long> images;
        // 널 허용
        private Long request;
    }

    // 생성/수정/삭제
    @Data
    @Builder
    public static class Request02 {
        private long id;
        @NotNull(message = "멘토링 일자 값이 비어있습니다.")
        private LocalDate date;
        @NotNull(message = "멘토링 시작 시간 값이 비어있습니다.")
        private LocalTime time;
        @NotNull(message = "멘토링 진행 시간 값이 비어있습니다.")
        private Integer duration;
        @NotNull(message = "멘토링 진행 장소 값이 비어있습니다.")
        private String location;
        @NotNull(message = "멘토링 진행 내용이 비어있습니다.")
        private String content;
        @NotNull(message = "멘토링 대상 멘티 목록이 NULL 입니다.")
        private List<Mentee> mentees;
        @NotNull(message = "증빙자료 이미지 목록이 NULL 입니다.")
        private List<Long> images;
        @NotNull(message = "증빙자료 요청 구분 값이 비어있습니다.")
        private Integer action; // 0 : None, 1 : create/update, -1 : delete
    }

    // 상세 응답
    @Data
    @Builder
    public static class Response01 {
        private Long id;
        private LocalDate date;
        private LocalTime time;
        private Integer duration;
        private String location;
        private String content;
        private List<Mentee> mentees;
        private List<FileResponse.Info> images;
        private Integer action; // 0 : None, 1 : create/update, -1 : delete
    }
}
