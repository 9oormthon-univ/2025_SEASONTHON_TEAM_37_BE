package rebound.backend.post.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import rebound.backend.post.entity.Comment;
import rebound.backend.post.entity.CommentStatus;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 1) 글의 전체 댓글(루트+대댓글) — 시간 오름차순, 무한스크롤용
    Slice<Comment> findByPostIdAndStatusOrderByCreatedAtAsc(
            Long postId, CommentStatus status, Pageable pageable);

    // 2) 루트 댓글만 — 쓰레드 UI에서 1차 목록
    Slice<Comment> findByPostIdAndParentCommentIdIsNullAndStatusOrderByCreatedAtAsc(
            Long postId, CommentStatus status, Pageable pageable);

    // 3) 특정 루트의 대댓글만 — 쓰레드 UI에서 펼칠 때
    Slice<Comment> findByPostIdAndParentCommentIdAndStatusOrderByCreatedAtAsc(
            Long postId, Long parentCommentId, CommentStatus status, Pageable pageable);
}
