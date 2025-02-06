package pheonix.classconnect.backend.course.model;

import lombok.Builder;
import lombok.Data;
import pheonix.classconnect.backend.course.entity.ProfessorEntity;

public class ProfessorDTO {
    @Data
    @Builder
    public static class Professor {
        private Long id;
        private String name;
        private String professorNo;

        public static Professor fromEntity(ProfessorEntity entity) {
            return Professor.builder()
                    .id(entity.getId())
                    .name(entity.getName())
                    .professorNo(entity.getProfessorNo())
                    .build();
        }
    }


}
