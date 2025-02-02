package pheonix.classconnect.backend.com.department.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Table(name = "Department")
@Getter @Setter
@Builder
@AllArgsConstructor @NoArgsConstructor
public class DepartmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Min(1) @Max(99)
    @Column(name="id", length=2, columnDefinition = "TINYINT UNSIGNED")
    private Integer id;

    @Column(nullable = false, length=20)
    private String name;
}
