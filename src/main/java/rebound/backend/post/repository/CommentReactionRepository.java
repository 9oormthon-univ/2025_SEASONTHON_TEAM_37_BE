// path: src/main/java/rebound/backend/post/repository/CommentReactionRepository.java
package rebound.backend.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rebound.backend.post.entity.CommentReaction;
import rebound.backend.post.entity.ReactionType;

public interface CommentReactionRepository extends JpaRepository<CommentReaction, Long> {
    boolean existsByCommentIdAndMemberIdAndType(Long commentId, Long memberId, ReactionType type);
    long deleteByCommentIdAndMemberIdAndType(Long commentId, Long memberId, ReactionType type);
    long countByCommentIdAndType(Long commentId, ReactionType type); // 필요 시 사용
}
