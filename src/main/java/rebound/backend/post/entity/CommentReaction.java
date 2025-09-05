// path: src/main/java/rebound/backend/post/entity/CommentReaction.java
package rebound.backend.post.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "comment_reaction",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_comment_member_type", columnNames = {"comment_id","member_id","type"}
        ),
        indexes = {
                @Index(name = "ix_comment_reaction_comment", columnList = "comment_id"),
                @Index(name = "ix_comment_reaction_comment_type", columnList = "comment_id,type")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CommentReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reaction_id")
    private Long id;

    @Column(name = "comment_id", nullable = false)
    private Long commentId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 16)
    private ReactionType type; // HEART

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
