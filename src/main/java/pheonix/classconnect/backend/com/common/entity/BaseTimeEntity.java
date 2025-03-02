package pheonix.classconnect.backend.com.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseTimeEntity {
    @Column(name = "created_date", updatable = false)
    private LocalDate createdDate;

    @Column(name = "created_time", updatable = false)
    private LocalTime createdTime;

    @Column(name = "updated_date")
    private LocalDate updatedDate;

    @Column(name = "updated_time")
    private LocalTime updatedTime;


    @PrePersist
    void createdAt() {
        this.createdDate = LocalDate.now();
        this.createdTime = LocalTime.now();
        this.updatedDate = LocalDate.now();
        this.updatedTime = LocalTime.now();
    }

    @PreUpdate
    void updatedAt(){
        this.updatedDate = LocalDate.now();
        this.updatedTime = LocalTime.now();
    }
}
