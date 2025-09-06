// path: src/main/java/rebound/backend/post/entity/CommentReaction.java
package rebound.backend.post.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name="comment_reaction",
        uniqueConstraints=@UniqueConstraint(name="uk_comment_member_type",
                columnNames={"comment_id","member_id","type"}),
        indexes = {@Index(name="ix_comment_reaction_comment", columnList="comment_id")}
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CommentReaction {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "id")              // ★ DB에 있는 컬럼명(id)로 고정
    private Long id;

    @Column(name="comment_id", nullable=false) private Long commentId;
    @Column(name="member_id",  nullable=false) private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name="type", nullable=false, length=16) private ReactionType type;

    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false) private Instant createdAt;
}
