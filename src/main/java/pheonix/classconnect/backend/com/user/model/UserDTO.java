package pheonix.classconnect.backend.com.user.model;

import lombok.Builder;
import lombok.Data;
import pheonix.classconnect.backend.com.auth.model.AuthorityDTO;
import pheonix.classconnect.backend.com.department.model.DepartmentDTO;
import pheonix.classconnect.backend.com.user.entity.UserEntity;

import java.util.Set;
import java.util.stream.Collectors;

public class UserDTO {
    @Data
    @Builder
    public static class User {
        private Long id;
        private String studentNo;
        private String email;
        private String name;
        private Short activeStatus;
        private DepartmentDTO.DepartmentInfo department;
        private Set<AuthorityDTO.AuthorityInfo> authorities;

        public static User fromEntity(UserEntity entity) {
            return User.builder()
                    .id(entity.getId())
                    .studentNo(entity.getStudentNo())
                    .email(entity.getEmail())
                    .name(entity.getName())
                    .activeStatus(entity.getActiveState())
                    .department(DepartmentDTO.DepartmentInfo.fromEntity(entity.getDepartment()))
                    .authorities(entity.getAuthorities().stream().map(AuthorityDTO.AuthorityInfo::fromEntity).collect(Collectors.toSet()))
                    .build();
        }

    }

    @Data
    @Builder
    public static class Signup {
        private String name;
        private String email;
        private String studentCode;
        private String department;
    }

    @Data
    @Builder
    public static class Create {
        private String email;
        private String studentNo;
        private String name;
        private String departmentName;
        private Set<Short> authorities;
        private Short activeStatus;
    }

    @Data
    @Builder
    public static class Response01 {
        private Long id;
        private String name;
        private String email;
        private String studentNo;
        private Short courseRole;
    }
}
