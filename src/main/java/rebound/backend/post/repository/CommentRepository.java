// path: src/main/java/rebound/backend/post/repository/CommentRepository.java
package rebound.backend.post.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import rebound.backend.post.entity.Comment;
import rebound.backend.post.entity.CommentStatus;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 무한스크롤(전체 댓글)
    Slice<Comment> findByPostIdAndStatusOrderByCreatedAtAsc(Long postId, CommentStatus status, Pageable pageable);

    // 루트 댓글만
    Slice<Comment> findByPostIdAndParentCommentIdIsNullAndStatusOrderByCreatedAtAsc(
            Long postId, CommentStatus status, Pageable pageable);

    // 특정 루트의 대댓글만
    Slice<Comment> findByPostIdAndParentCommentIdAndStatusOrderByCreatedAtAsc(
            Long postId, Long parentCommentId, CommentStatus status, Pageable pageable);

    // 자식(삭제되지 않은) 존재 여부
    boolean existsByParentCommentIdAndStatusNot(Long parentCommentId, CommentStatus status);

    // 소프트 삭제: 상태 전환 + 본문 마스킹 + 삭제시각 기록
    @Modifying
    @Query("""
        update Comment c
           set c.status = rebound.backend.post.entity.CommentStatus.DELETED,
               c.content = '[삭제된 댓글입니다]',
               c.deletedAt = CURRENT_TIMESTAMP
         where c.commentId = :id
    """)
    int softDelete(@Param("id") Long id);
}
