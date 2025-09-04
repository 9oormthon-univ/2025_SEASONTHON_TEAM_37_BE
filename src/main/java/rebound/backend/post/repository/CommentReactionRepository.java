package rebound.backend.post.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import rebound.backend.post.entity.CommentReaction;
import rebound.backend.post.entity.ReactionType;

public interface CommentReactionRepository extends JpaRepository<CommentReaction, Long> {
    boolean existsByCommentIdAndMemberIdAndType(Long cId, Long mId, ReactionType t);
    long deleteByCommentIdAndMemberIdAndType(Long cId, Long mId, ReactionType t);
}
