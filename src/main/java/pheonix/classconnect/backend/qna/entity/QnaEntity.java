package pheonix.classconnect.backend.qna.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pheonix.classconnect.backend.com.common.entity.BaseTimeEntity;
import pheonix.classconnect.backend.com.user.entity.UserEntity;

import java.time.LocalDate;
import java.time.LocalTime;

@Table(name = "QNA")
@Entity(name = "QNA")
@Getter
@Builder
@AllArgsConstructor @NoArgsConstructor
public class QnaEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", length = 80)
    private String title;

    @Column(name = "pub_gubun")
    private Short publishType;

    @Column(name = "question", length = 2000)
    private String question;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "questioner_id", referencedColumnName = "id")
    private UserEntity questioner;

    @Column(name = "answer", length = 2000)
    private String answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answerer_id", referencedColumnName = "id")
    private UserEntity answerer;

    @Column(name = "answer_date")
    private LocalDate answerDate;

    @Column(name = "answer_time")
    private LocalTime answerTime;

    @Column(name = "is_answered")
    private Boolean answered;

    public void addAnswer(String answer, UserEntity answerer) {
        this.answer = answer;
        this.answerer = answerer;
        this.answerDate = LocalDate.now();
        this.answerTime = LocalTime.now();
        this.answered = true;
    }

}
