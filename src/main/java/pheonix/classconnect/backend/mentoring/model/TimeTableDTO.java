package pheonix.classconnect.backend.mentoring.model;

import lombok.Builder;
import lombok.Data;
import pheonix.classconnect.backend.com.user.model.UserDTO;
import pheonix.classconnect.backend.mentoring.entity.TimeTableEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class TimeTableDTO {

    @Data
    @Builder
    public static class TimeTable {
        private UserDTO.User user;
        private Integer version;
        private Integer serNo;
        private LocalDate startDate;
        private LocalDate endDate;
        private Short day;
        private LocalTime startTime;
        private LocalTime endTime;
        private Short site;

        public static TimeTable fromEntity(TimeTableEntity entity) {
            return TimeTable.builder()
                    .user(UserDTO.User.fromEntity(entity.getUser()))
                    .version(entity.getId().getVer())
                    .serNo(entity.getId().getSerNo())
                    .startDate(entity.getStartDate())
                    .endDate(entity.getEndDate())
                    .day(entity.getDay())
                    .startTime(entity.getStartTime())
                    .endTime(entity.getEndTime())
                    .site(entity.getSite())
                    .build();
        }
    }

    @Data
    @Builder
    public static class Create {
        private Long userId;
        private LocalDate startDate;
        private LocalDate endDate;
        private List<ScheduleDTO.Weekly> schedules;
    }

    @Data
    @Builder
    public static class Request01 {
        private LocalDate startDate;
        private LocalDate endDate;
        private List<ScheduleDTO.Request01> timeSlots;
    }

    @Data
    @Builder
    public static class Response21 {
        private Integer dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;
        private Short mentoringType;
    }
}
