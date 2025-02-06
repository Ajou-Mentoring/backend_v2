package pheonix.classconnect.backend.course.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity(name= "Professor")
@Table(name = "Professor")
@Getter
@Builder
@AllArgsConstructor @NoArgsConstructor
public class ProfessorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "professor_no", length = 9, unique = true)
    private String professorNo;

    @Column(name = "email", length = 50, unique = true)
    private String email;

    @Column(name = "name", length = 50)
    private String name;
}
