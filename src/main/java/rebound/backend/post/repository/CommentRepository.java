package rebound.backend.post.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import rebound.backend.post.entity.Comment;
import rebound.backend.post.entity.CommentStatus;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    boolean existsByParentCommentIdAndStatusNot(Long parentCommentId, CommentStatus status);

    // [게시글 상세] 해당 게시글의 전체 댓글(루트+대댓글) — 상태 필터, 오래된 순
    Slice<Comment> findByPostIdAndStatusOrderByCreatedAtAsc(Long postId,
                                                            CommentStatus status,
                                                            Pageable pageable);

    // 루트 댓글만
    Slice<Comment> findByPostIdAndParentCommentIdIsNullAndStatusOrderByCreatedAtAsc(Long postId,
                                                                                    CommentStatus status,
                                                                                    Pageable pageable);

    // 특정 댓글의 대댓글만
    Slice<Comment> findByPostIdAndParentCommentIdAndStatusOrderByCreatedAtAsc(Long postId,
                                                                              Long parentCommentId,
                                                                              CommentStatus status,
                                                                              Pageable pageable);

    // [마이페이지] 내가 단 댓글(최신순)
    Slice<Comment> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    // [삭제 로직] 자식댓글 존재 여부 확인(조건부 소프트삭제용)
    boolean existsByParentCommentId(Long parentCommentId);

    // 내 댓글만 조회
    Optional<Comment> findByCommentIdAndMemberId(Long commentId, Long memberId);

    // 마이페이지: 내가 댓글 단 게시글 ID목록(최신 댓글 기준, 중복 제거)
    @Query("""
           select c.postId
           from Comment c
           where c.memberId = :memberId and c.status <> 'DELETED'
           group by c.postId
           order by max(c.createdAt) desc
           """)
    Slice<Long> findCommentedPostIds(@Param("memberId") Long memberId, Pageable pageable);

    // 상태까지 필터링하는 버전 (마이페이지용)
    @Query("""
       select c.postId
       from Comment c
       where c.memberId = :memberId and c.status = :status
       group by c.postId
       order by max(c.createdAt) desc
       """)
    Slice<Long> findCommentedPostIds(Long memberId, CommentStatus status, Pageable pageable);

    // like_count 토글직후 동기화
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Comment c set c.likeCount = :cnt where c.commentId = :commentId")
    int updateLikeCount(@Param("commentId") Long commentId, @Param("cnt") int cnt);

    // 소프트삭제
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Comment c set c.status = rebound.backend.post.entity.CommentStatus.DELETED, c.content='[삭제된 댓글]' where c.commentId = :commentId")
    int softDelete(@Param("commentId") Long commentId);
}
