package pheonix.classconnect.backend.security.model;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

public class UserDetailDTO {
    @Data
    @Builder
    public static class User {
        private String username;
        private String password;
        private Set<Authority> authorities;
    }

    @Data
    @Builder
    public static class Authority {
        private String authority;
    }
}
