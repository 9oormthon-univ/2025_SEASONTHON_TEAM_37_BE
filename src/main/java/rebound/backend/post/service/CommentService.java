// path: src/main/java/rebound/backend/post/service/CommentService.java
package rebound.backend.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rebound.backend.post.entity.*;
import rebound.backend.post.repository.CommentReactionRepository;
import rebound.backend.post.repository.CommentRepository;
import rebound.backend.utils.InteractionAuth;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepo;
    private final CommentReactionRepository reactionRepo;

    private Long me() { return InteractionAuth.currentMemberId(); }

    public Slice<Comment> list(Long postId, int page, int size) {
        return commentRepo.findByPostIdAndStatusOrderByCreatedAtAsc(
                postId, CommentStatus.PUBLIC, PageRequest.of(page, size));
    }

    @Transactional
    public Comment create(Long postId, String content, boolean isAnonymous, Long parentId) {
        var c = Comment.builder()
                .postId(postId)
                .memberId(me())
                .content(content)
                .isAnonymous(isAnonymous)
                .parentCommentId(parentId)
                .status(CommentStatus.PUBLIC)
                .build();
        return commentRepo.save(c);
    }

    /** 자식 있으면 소프트 삭제, 없으면 하드 삭제 */
    @Transactional
    public void delete(Long commentId) {
        boolean hasChildren = commentRepo.existsByParentCommentIdAndStatusNot(
                commentId, CommentStatus.DELETED);

        if (hasChildren) {
            commentRepo.softDelete(commentId);     // 자리표시 + 상태만 변경
        } else {
            commentRepo.deleteById(commentId);     // 실제 삭제
        }
    }

    /** 댓글 하트 토글 (빌더 사용, 동시요청 대비) */
    @Transactional
    public boolean toggleHeart(Long commentId) {
        Long m = me();
        boolean exists = reactionRepo.existsByCommentIdAndMemberIdAndType(commentId, m, ReactionType.HEART);
        if (exists) {
            reactionRepo.deleteByCommentIdAndMemberIdAndType(commentId, m, ReactionType.HEART);
            return false;
        } else {
            try {
                reactionRepo.save(
                        CommentReaction.builder()
                                .commentId(commentId)      // ← 빌더 사용
                                .memberId(m)
                                .type(ReactionType.HEART)
                                .build()
                );
                return true;
            } catch (DataIntegrityViolationException dup) {
                // 동시 요청으로 이미 생긴 경우 → 켜진 상태로 간주
                return true;
            }
        }
    }
}
