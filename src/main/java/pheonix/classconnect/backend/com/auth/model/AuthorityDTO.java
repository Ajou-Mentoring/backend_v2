package pheonix.classconnect.backend.com.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.Builder;
import lombok.Data;
import pheonix.classconnect.backend.com.auth.entity.AuthorityEntity;
import pheonix.classconnect.backend.com.department.entity.DepartmentEntity;
import pheonix.classconnect.backend.com.user.entity.UserEntity;

import java.util.Set;

public class AuthorityDTO {
    @Data
    @Builder
    public static class AuthorityInfo {
        private Short code;
        private String korName;
        private String engName;

        public static AuthorityInfo fromEntity(AuthorityEntity entity) {
            return AuthorityInfo.builder()
                    .code(entity.getCode())
                    .korName(entity.getKorName())
                    .engName(entity.getEngName())
                    .build();
        }

    }
}
