package pheonix.classconnect.backend.com.common.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pheonix.classconnect.backend.com.common.constants.CursorType;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class PageResponse<T> {

    private List<T> items;
    private boolean hasNext;

    private Integer nextCursorId;

    private CursorType type = CursorType.NORMAL;
}
