package pheonix.classconnect.backend.com.department.model;

import lombok.*;
import pheonix.classconnect.backend.com.department.entity.DepartmentEntity;

public class DepartmentDTO {

    @Data
    @Builder
    public static class DepartmentInfo{
        private Integer id;
        private String name;

        public static DepartmentInfo fromEntity(DepartmentEntity department) {
            return new DepartmentInfo(department.getId(), department.getName());
        }
    }

}
