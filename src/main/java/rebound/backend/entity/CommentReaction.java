package rebound.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name="comment_reaction",
        uniqueConstraints=@UniqueConstraint(name="uk_comment_member_type", //댓글 하트 토글 중복 방지
                columnNames={"comment_id","member_id","type"}),
        indexes = {@Index(name="ix_comment_reaction_comment", columnList="comment_id")}
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CommentReaction {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name="comment_id", nullable=false) private Long commentId;
    @Column(name="member_id",  nullable=false) private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name="type", nullable=false, length=16) private ReactionType type; // HEART

    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false) private Instant createdAt;
}
