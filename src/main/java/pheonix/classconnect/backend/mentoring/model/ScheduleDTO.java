package pheonix.classconnect.backend.mentoring.model;

import lombok.Builder;
import lombok.Data;
import pheonix.classconnect.backend.com.user.model.UserDTO;
import pheonix.classconnect.backend.mentoring.entity.ScheduleEntity;

import java.time.LocalDate;
import java.time.LocalTime;

public class ScheduleDTO {

    @Data
    @Builder
    public static class Schedule {
        private UserDTO.User user;
        private LocalDate date;
        private Integer serNo;
        private LocalTime startTime;
        private LocalTime endTime;
        private Short site;

        public static Schedule fromEntity(ScheduleEntity entity) {
            return Schedule.builder()
                    .user(UserDTO.User.fromEntity(entity.getUser()))
                    .date(entity.getId().getDate())
                    .serNo(entity.getId().getSerNo())
                    .startTime(entity.getStartTime())
                    .endTime(entity.getEndTime())
                    .site(entity.getSite())
                    .build();
        }
    }

    @Data
    @Builder
    public static class Create {
        private LocalTime startTime;
        private LocalTime endTime;
        private Short mentoringType;
    }

    @Data
    @Builder
    public static class Weekly {
        private Integer dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;
        private Short site;
    }

    @Data
    @Builder
    public static class Request01 {
        private Integer dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;
        private Short mentoringType;
    }

    @Data
    @Builder
    public static class Request02 {
        private LocalTime startTime;
        private LocalTime endTime;
        private Short mentoringType;
    }

    @Data
    @Builder
    public static class Response01 {
        private LocalTime startTime;
        private LocalTime endTime;
        private Short site;
    }

    @Data
    @Builder
    public static class Response31 {
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
        private Short mentoringType;
    }

    @Data
    @Builder
    public static class Response32 {
        private Integer serNo;
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
        private Short mentoringType;
    }
}

