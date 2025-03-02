package pheonix.classconnect.backend.mentoring.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ScheduleId implements Serializable {
    private Long userId;
    private LocalDate date;
    private Integer serNo;

    // equals()와 hashCode() 메소드
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScheduleId that = (ScheduleId) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(date, that.date) &&
                Objects.equals(serNo, that.serNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, date, serNo);
    }
}
