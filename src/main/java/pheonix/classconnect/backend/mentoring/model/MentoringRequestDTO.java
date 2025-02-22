package pheonix.classconnect.backend.mentoring.model;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import pheonix.classconnect.backend.com.attachment.model.File;
import pheonix.classconnect.backend.com.attachment.model.response.FileResponse;
import pheonix.classconnect.backend.com.user.model.UserDTO;
import pheonix.classconnect.backend.course.model.CourseDTO;
import pheonix.classconnect.backend.mentoring.entity.MentoringRequestEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MentoringRequestDTO {
    @Data
    @Builder
    public static class MentoringRequest {
        private Long id;
        private UserDTO.User mentor;
        private UserDTO.User requester;
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
        private Short status;
        private Short site;
        private String content;
        private String comment;
        private CourseDTO.Course course;
        private Map<String, Object> mentees;
        private Boolean registered;
        private List<File> images;
        private List<File> files;

        public static MentoringRequest fromEntity(MentoringRequestEntity entity) {
            return MentoringRequest.builder()
                    .id(entity.getId())
                    .mentor(entity.getMentor() == null ? null : UserDTO.User.fromEntity(entity.getMentor()))
                    .requester(entity.getRequester() == null ? null : UserDTO.User.fromEntity(entity.getRequester()))
                    .date(entity.getDate())
                    .startTime(entity.getStartTime())
                    .endTime(entity.getEndTime())
                    .status(entity.getStatus())
                    .course(entity.getCourse() == null ? null : CourseDTO.Course.fromEntity(entity.getCourse()))
                    .content(entity.getContent())
                    .site(entity.getSite())
                    .mentees(entity.getMentees())
                    .registered(entity.isRegistered())
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
        private Long mentorId;
        private Long requesterId;
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
        private Short site;
        private String content;
        private Long courseId;
        private Map<String, Object> mentees;
        private List<Long> images;
        private List<Long> files;

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
        private Map<String, Object> mentees;
        private Short site;
        private String content;
        private List<Long> images;
        private List<Long> files;
    }

    // 생성
    @Data
    @Builder
    public static class Request01 {
        @NotNull(message = "날짜 값이 비어있습니다.")
        private LocalDate date;
        @NotNull(message = "멘토 아이디가 비어있습니다.")
        private Long mentorId;
        @NotNull(message = "멘토링 시작 시간이 비어있습니다.")
        private LocalTime startTime;
        @NotNull(message = "멘토링 종료 시간이 비어있습니다.")
        private LocalTime endTime;
        @NotNull(message = "멘토링 방식이 지정되지 않았습니다.")
        private Short mentoringType;
        @NotNull(message = "신청 내용이 비어있습니다.")
        private String content;
        @NotNull(message = "멘티 정보가 비어있습니다.")
        private List<Mentee> mentees;
        private List<Long> images;
        private List<Long> files;
    }

    // 요청 수정
    @Data
    @Builder
    public static class Request02 {
        @NotNull(message = "신청 내용이 비어있습니다.")
        private String content;
        @NotNull(message = "멘티 정보가 비어있습니다.")
        private List<Mentee> mentees;
        @NotNull(message = "멘토링 방식이 지정되지 않았습니다.")
        private Short mentoringType;
        private List<Long> images;
        private List<Long> files;
    }

    // 상태 변경
    @Data
    @Builder
    public static class Request03 {
        @NotNull(message = "승인/반려/취소 사유가 비어있습니다.")
        private String comment;
        @NotNull(message = "처리 구분 값이 비어있습니다.")
        private Short action;
    }

    // 상세 응답
    @Data
    @Builder
    public static class Response01 {
        private Long id;
        private LocalDate date;
        private UserDTO.Response01 mentor;
        private LocalTime startTime;
        private LocalTime endTime;
        private Short mentoringType;
        private Short status;
        private String content;
        private List<Mentee> mentees;
        private boolean isRegistered;
        private List<FileResponse.Info> images;
        private List<FileResponse.Info> files;
    }

    // 리스트 응답
    @Data
    @Builder
    public static class Response10 {
        private Long id;
        private LocalDate date;
        private UserDTO.Response03 mentor;
        private LocalTime startTime;
        private LocalTime endTime;
        private Short mentoringType;
        private List<UserDTO.Response03> mentees;
    }
}
