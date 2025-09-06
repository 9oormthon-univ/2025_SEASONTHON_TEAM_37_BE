package rebound.backend.post.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "post_reaction",
        uniqueConstraints = @UniqueConstraint(  //유니크제약 -> 같은 사용자가 같은 글에 같은 반응을 두번 만들 수 없도록.
                name = "uk_post_type_member", columnNames = {"post_id","type","member_id"}
        ),
        indexes = {
                @Index(name="ix_post_reaction_post", columnList = "post_id"),
                @Index(name="ix_post_reaction_member", columnList = "member_id")
        }
        )
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PostReaction {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reaction_id") private Long reactionId;

    @Column(name = "post_id", nullable = false)   private Long postId;
    @Column(name = "member_id", nullable = false) private Long memberId; //반응한 사용자 id

    @Enumerated(EnumType.STRING) //enum을 문자열로 저장(가독성)
    @Column(name = "type", nullable = false, length = 16) private ReactionType type;

    @CreationTimestamp //INSERT시 DB시간으로 자동 입력
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
}
