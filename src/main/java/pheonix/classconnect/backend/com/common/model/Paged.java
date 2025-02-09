package pheonix.classconnect.backend.com.common.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Paged<T> {
    private Integer currentPage;
    private Integer numberOfElements;
    private Integer size;
    private Integer totalPages;
    private Long totalElements;
    private List<T> items;
}
