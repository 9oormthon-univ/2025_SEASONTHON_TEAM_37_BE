package rebound.backend.post.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rebound.backend.post.entity.PostReaction;
import rebound.backend.post.entity.ReactionType;

import java.util.List;
import java.util.Set;
import java.util.Map;

public interface PostReactionRepository extends JpaRepository<PostReaction, Long> {

    boolean existsByPostIdAndMemberIdAndType(Long postId, Long memberId, ReactionType type);

    long deleteByPostIdAndMemberIdAndType(Long postId, Long memberId, ReactionType type);

    long countByPostIdAndType(Long postId, ReactionType type);

    // 특정 사용자가 받은 총 좋아요 수를 조회하는 메서드 (새로 추가)
    long countByMemberIdAndType(Long memberId, ReactionType type);

    // 마이페이지: 내가 하트 누른 글 postId 목록
    @Query("""
           select r.postId
           from PostReaction r
           where r.memberId = :memberId and r.type = :type
           order by r.createdAt desc, r.reactionId desc
           """)
    Slice<Long> findPostIdsReactedBy(@Param("memberId") Long memberId,
                                     @Param("type") ReactionType type,
                                     Pageable pageable);

    // 목록 한 페이지의 여러 postId에 대한 좋아요수 벌크 집계
    @Query("""
           select pr.postId as postId, count(pr) as cnt
           from PostReaction pr
           where pr.type = :type and pr.postId in :postIds
           group by pr.postId
           """)
    List<PostCountRow> countReactionsByPostIds(@Param("type") ReactionType type,
                                               @Param("postIds") List<Long> postIds);

    // 로그인 유저가 그 페이지의 어떤 글들에 좋아요 눌렀는지
    @Query("""
           select pr.postId
           from PostReaction pr
           where pr.type = :type and pr.memberId = :memberId and pr.postId in :postIds
           """)
    List<Long> findPostIdsLikedBy(@Param("type") ReactionType type,
                                  @Param("memberId") Long memberId,
                                  @Param("postIds") List<Long> postIds);

    @Query("SELECT pr.postId FROM PostReaction pr WHERE pr.memberId = :memberId AND pr.postId IN :postIds AND pr.type = :type")
    Set<Long> findLikedPostIdsByMemberIdAndPostIds(@Param("memberId") Long memberId, @Param("postIds") List<Long> postIds, @Param("type") ReactionType type);

    interface PostCountRow {
        Long getPostId();
        Long getCnt();
    }

    @Query("SELECT pr.postId, COUNT(pr) FROM PostReaction pr WHERE pr.postId IN :postIds AND pr.type = :type GROUP BY pr.postId")
    Map<Long, Long> countByPostIds(@Param("postIds") List<Long> postIds, @Param("type") ReactionType type);

    // 주어진 사용자 목록이 받은 총 좋아요 수를 한 번에 조회하는 메서드
    @Query("SELECT pr.memberId, COUNT(pr) FROM PostReaction pr WHERE pr.memberId IN :memberIds AND pr.type = :type GROUP BY pr.memberId")
    List<Object[]> countTotalLikesByMemberIds(@Param("memberIds") List<Long> memberIds, @Param("type") ReactionType type);


    @Query("""
    SELECT pr
    FROM PostReaction pr
    WHERE pr.postId = :postId
    """)
    List<PostReaction> countTotalLikeOfPost(@Param("postId") Long postId);
}
