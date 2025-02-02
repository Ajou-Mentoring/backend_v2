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
    public static class UserInfo {
        private Long id;
        private String studentNo;
        private String email;
        private String name;
        private Short activeState;
        private DepartmentDTO.DepartmentInfo department;
        private Set<AuthorityDTO.AuthorityInfo> authorities;

        public static UserInfo fromEntity(UserEntity entity) {
            return UserInfo.builder()
                    .id(entity.getId())
                    .studentNo(entity.getStudentNo())
                    .email(entity.getEmail())
                    .name(entity.getName())
                    .activeState(entity.getActiveState())
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
        private String studentNo;
        private Integer department;
    }

    @Data
    @Builder
    public static class FindOne {
        private Long id;
        private String email;
        private String studentNo;
    }
}
