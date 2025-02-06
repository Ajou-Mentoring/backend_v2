package pheonix.classconnect.backend.course.model;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import pheonix.classconnect.backend.com.user.model.UserDTO;
import pheonix.classconnect.backend.course.entity.CourseEntity;
import pheonix.classconnect.backend.course.entity.UserCourseEntity;

import java.time.LocalDateTime;
import java.util.List;

public class CourseDTO {
    @Data
    @Builder
    public static class Course {
        private Long id;

        private String name;

        private String courseCode;

        private String year;

        private Short semester;

        private ProfessorDTO.Professor professor;

        private List<UserDTO.User> members;

        private LocalDateTime createdAt;

        private LocalDateTime updatedAt;

        public static Course fromEntity(CourseEntity entity) {
            return Course.builder()
                    .id(entity.getId())
                    .name(entity.getName())
                    .courseCode(entity.getCode())
                    .year(entity.getYear())
                    .semester(entity.getSemester())
                    .professor(entity.getProfessor() != null ? ProfessorDTO.Professor.fromEntity(entity.getProfessor()) : null)
                    .members(entity.getStudents().stream()
                            .map(UserCourseEntity::getUser)
                            .map(UserDTO.User::fromEntity).toList())
                    .createdAt(LocalDateTime.of(entity.getCreatedDate(), entity.getCreatedTime()))
                    .updatedAt(LocalDateTime.of(entity.getUpdatedDate(), entity.getUpdatedTime()))
                    .build();
        }
    }

    @Data
    @Builder
    public static class Create {
        private String year;
        private Short semester;
        private String name;
        private String code;
        private Long professorId;
    }

    @Data
    @Builder
    public static class FetchByYearAndSemesterAndMemberId {
        @NotNull(message = "수업 연도를 입력해주세요.")
        private String year;
        @NotNull(message = "수업 학기를 입력해주세요.")
        private Short semester;
        @NotNull(message = "멤버 아이디를 입력해주세요.")
        private Long memberId;
    }
}
