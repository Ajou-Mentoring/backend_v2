package pheonix.classconnect.backend.com.department.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Table(name = "Department")
@Getter
@Builder
@AllArgsConstructor @NoArgsConstructor
public class DepartmentEntity {
    @Id
    @Column(columnDefinition = "TINYINT UNSIGNED")
    private Integer id;

    @Column(nullable = false, length=20)
    private String name;
}
