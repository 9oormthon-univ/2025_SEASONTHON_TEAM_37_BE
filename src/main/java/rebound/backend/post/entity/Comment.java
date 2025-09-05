package rebound.backend.post.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;

@Entity @Table(name="comment",
        indexes = {
                @Index(name="ix_comment_post", columnList="post_id"),
                @Index(name="ix_comment_parent", columnList="parent_comment_id")
                @Index(name = "ix_comment_post_created", columnList = "post_id,created_at")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Comment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="comment_id") private Long commentId;

    @Column(name="post_id", nullable=false)   private Long postId;
    @Column(name="member_id", nullable=false) private Long memberId;

    @Column(name="parent_comment_id") private Long parentCommentId; // 대댓글(SELF FK)

    @Lob @Column(name="content", nullable=false, columnDefinition="TEXT") private String content;

    @Column(name="is_anonymous", nullable=false) private boolean isAnonymous = true; //익명/실명

    // 예약어 회피
    @Column(name="like_count", nullable=false) private int likeCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable=false, length=16) private CommentStatus status = CommentStatus.PUBLIC; // PUBLIC/HIDDEN/DELETED

    @CreationTimestamp @Column(name="created_at", updatable=false) private Instant createdAt;
    @UpdateTimestamp   @Column(name="updated_at") private Instant updatedAt;
    @Column(name="deleted_at") private Instant deletedAt;
}
