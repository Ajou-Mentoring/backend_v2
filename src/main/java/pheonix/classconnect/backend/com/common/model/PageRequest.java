package pheonix.classconnect.backend.com.common.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class PageRequest {
    private Long cursorId = 0L;
    private Integer page = 1;
    private Integer size = 15;

    public PageRequest(Long cursorId, Integer page, Integer size){
        this.cursorId = cursorId;
        this.page = page > 1 ? page : 1;
        this.size = size > 0 ? size : 15;
    }

    public PageRequest(Integer page, Integer size){
        this.cursorId = 0L;
        this.page = page;
        this.size = size;
    }

    public PageRequest(Integer page){
        this.cursorId = 0L;
        this.page = page;
        this.size = 15;
    }
}
