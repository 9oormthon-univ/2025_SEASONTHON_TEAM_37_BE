package rebound.backend.post.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "post_content",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_post_content_post", columnNames = "post_id")
        }
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "content_id")
    private Long contentId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_post_content_post"))
    private Post post;

    @Lob
    @Column(name = "situation_content")
    private String situationContent; // 상황

    @Lob
    @Column(name = "failure_content")
    private String failureContent;   // 실패

    @Lob
    @Column(name = "learning_content")
    private String learningContent;  // 배움

    @Lob
    @Column(name = "next_step_content")
    private String nextStepContent;  // 다음 단계
}