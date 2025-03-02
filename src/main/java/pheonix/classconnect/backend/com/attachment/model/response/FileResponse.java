package pheonix.classconnect.backend.com.attachment.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pheonix.classconnect.backend.com.attachment.model.File;

public class FileResponse {
    @Data
    @Builder
    @AllArgsConstructor @NoArgsConstructor
    public static class Info {
        private Long id;
        private String name;
        private String path;
        private Long size;

        public static Info fromFile(File file) {
            return Info.builder()
                    .id(file.getId())
                    .name(file.getOriginalFileName())
                    .path(file.getPath())
                    .size(file.getSize())
                    .build();
        }
    }
}
