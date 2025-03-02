package pheonix.classconnect.backend.mentoring.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter
@Builder
public class TimeTableId implements Serializable {
    private Long userId;
    private Integer ver;
    private Integer serNo;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeTableId that = (TimeTableId) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(ver, that.ver) &&
                Objects.equals(serNo, that.serNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, ver, serNo);
    }
}
