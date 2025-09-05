package rebound.backend.post.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import rebound.backend.post.entity.PostReaction;
import rebound.backend.post.entity.ReactionType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface PostReactionRepository extends JpaRepository<PostReaction, Long> {
    boolean existsByPostIdAndMemberIdAndType(Long postId, Long memberId, ReactionType type);
    long deleteByPostIdAndMemberIdAndType(Long postId, Long memberId, ReactionType type);
    long countByPostIdAndType(Long postId, ReactionType type); //최신집계반환(목록/버튼 옆 숫자 표시용)

   // 마이페이지: 내가 하트 누른 글 postId 목록 (최신순)
    @Query("""
           select r.postId
           from PostReaction r
           where r.memberId = :memberId and r.type = :type
           order by r.createdAt desc
           """)
    Slice<Long> findPostIdsReactedBy(@Param("memberId") Long memberId,
                                     @Param("type") ReactionType type,
                                     Pageable pageable);
}
