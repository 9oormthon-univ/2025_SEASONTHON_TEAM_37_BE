package rebound.backend.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import rebound.backend.entity.PostReaction;
import rebound.backend.entity.ReactionType;

public interface PostReactionRepository extends JpaRepository<PostReaction, Long> {
    boolean existsByPostIdAndMemberIdAndType(Long postId, Long memberId, ReactionType type);
    long deleteByPostIdAndMemberIdAndType(Long postId, Long memberId, ReactionType type);
    long countByPostIdAndType(Long postId, ReactionType type); //최신집계반환(목록/버튼 옆 숫자 표시용)
}
