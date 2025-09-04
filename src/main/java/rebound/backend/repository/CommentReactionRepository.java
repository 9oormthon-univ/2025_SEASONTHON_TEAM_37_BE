package rebound.backend.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import rebound.backend.entity.CommentReaction;
import rebound.backend.entity.ReactionType;

public interface CommentReactionRepository extends JpaRepository<CommentReaction, Long> {
    boolean existsByCommentIdAndMemberIdAndType(Long cId, Long mId, ReactionType t);
    long deleteByCommentIdAndMemberIdAndType(Long cId, Long mId, ReactionType t);
}
