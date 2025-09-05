package rebound.backend.post.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rebound.backend.post.entity.CommentReaction;
import rebound.backend.post.entity.ReactionType;

import java.util.Collection;
import java.util.List;

public interface CommentReactionRepository extends JpaRepository<CommentReaction, Long> {

    // 토글용 존재/삭제/단건 집계
    boolean existsByCommentIdAndMemberIdAndType(Long commentId, Long memberId, ReactionType type);
    long deleteByCommentIdAndMemberIdAndType(Long commentId, Long memberId, ReactionType type);
    long countByCommentIdAndType(Long commentId, ReactionType type);

    // [마이페이지] 내가 하트 누른 댓글 id 목록 (최신순)
    @Query("""
           select cr.commentId
           from CommentReaction cr
           where cr.memberId = :memberId and cr.type = :type
           order by cr.createdAt desc
           """)
    Slice<Long> findCommentIdsReactedBy(@Param("memberId") Long memberId,
                                        @Param("type") ReactionType type,
                                        Pageable pageable);

    // 여러 댓글 id에 대한 좋아요수 집계
    @Query("""
           select cr.commentId as commentId, count(cr) as cnt
           from CommentReaction cr
           where cr.type = :type and cr.commentId in :commentIds
           group by cr.commentId
           """)
    List<CommentCountRow> countReactionsByCommentIds(@Param("type") ReactionType type,
                                                     @Param("commentIds") Collection<Long> commentIds);

    // 로그인 사용자가 그 중 어떤 댓글들에 하트 눌렀는지 체크
    @Query("""
           select cr.commentId
           from CommentReaction cr
           where cr.memberId = :memberId and cr.type = :type and cr.commentId in :commentIds
           """)
    List<Long> findCommentIdsLikedBy(@Param("memberId") Long memberId,
                                     @Param("type") ReactionType type,
                                     @Param("commentIds") Collection<Long> commentIds);

    // 집계 결과 받기
    interface CommentCountRow {
        Long getCommentId();
        Long getCnt();
    }
}
