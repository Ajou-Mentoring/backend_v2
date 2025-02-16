package pheonix.classconnect.backend.mentoring.model;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import pheonix.classconnect.backend.com.attachment.model.File;
import pheonix.classconnect.backend.course.entity.CourseEntity;
import pheonix.classconnect.backend.mentoring.entity.MentoringRequestEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class MentoringRequestDTO {
    @Data
    @Builder
    public static class MentoringRequest {
        private Long id;
        private Long mentorId;
        private Long requesterId;
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
        private Short status;
        private String content;
        private String comment;
        private Long courseId;
        private Map<String, Object> mentees;
        private List<File> images;
        private List<File> files;

        public static MentoringRequest fromEntity(MentoringRequestEntity entity) {
            return MentoringRequest.builder()
                    .id(entity.getId())
                    .mentorId(entity.getMentor() == null ? null : entity.getMentor().getId())
                    .requesterId(entity.getRequester() == null ? null : entity.getRequester().getId())
                    .date(entity.getDate())
                    .startTime(entity.getStartTime())
                    .endTime(entity.getEndTime())
                    .status(entity.getStatus())
                    .courseId(entity.getCourse() == null ? null : entity.getCourse().getId())
                    //.mentees(entity.getMentees())
                    .build();
        }
    }

    @Data
    @Builder
    public static class Create {
        private Long mentorId;
        private Long requesterId;
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
        private String content;
        private Long courseId;
        private Map<String, Object> mentees;
        private List<Long> images;
        private List<Long> files;
    }

    @Data
    @Builder
    public static class Update {
        private Map<String, Object> mentees;
        private Short status;
        private String content;
        private List<Long> images;
        private List<Long> files;
    }

    // 생성
    @Data
    @Builder
    public static class Request01 {
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
        private String content;
        private Map<String, Object> mentees;
        private List<Long> images;
        private List<Long> files;
    }
}
