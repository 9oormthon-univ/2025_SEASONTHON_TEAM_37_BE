package rebound.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

@Entity
@Table(name = "post_bookmark",
        uniqueConstraints = @UniqueConstraint( // 유니크제약 -> 스크랩 중복 방지
                name = "uk_post_member", columnNames = {"post_id","member_id"}),
        indexes = {
                @Index(name="ix_post_bookmark_post", columnList = "post_id"),
                @Index(name="ix_post_bookmark_member", columnList = "member_id")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PostBookmark {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookmark_id") private Long bookmarkId;

    @Column(name = "post_id", nullable = false)   private Long postId;
    @Column(name = "member_id", nullable = false) private Long memberId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
}
