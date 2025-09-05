// path: src/main/java/rebound/backend/post/service/CommentService.java
package rebound.backend.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rebound.backend.post.entity.Comment;
import rebound.backend.post.entity.CommentReaction;
import rebound.backend.post.entity.CommentStatus;
import rebound.backend.post.entity.ReactionType;
import rebound.backend.post.repository.CommentReactionRepository;
import rebound.backend.post.repository.CommentRepository;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepo;
    private final CommentReactionRepository reactionRepo;

    private Long me() { return rebound.backend.utils.AuthUtils.currentMemberId(); }

    /** 글의 전체 댓글(루트+대댓글), PUBLIC만, 시간 오름차순, 무한스크롤 */
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
                .status(CommentStatus.PUBLIC) // 기본 공개
                .build();
        return commentRepo.save(c);
    }

    @Transactional
    public void delete(Long commentId) {
        // 간단 삭제(하드 딜리트). 소프트 삭제가 필요하면 status 갱신으로 변경
        commentRepo.deleteById(commentId);
    }

    /** 댓글 하트 토글 (동시요청 대비) */
    @Transactional
    public boolean toggleHeart(Long commentId) {
        Long m = me();
        boolean exists = reactionRepo.existsByCommentIdAndMemberIdAndType(commentId, m, ReactionType.HEART);
        if (exists) {
            reactionRepo.deleteByCommentIdAndMemberIdAndType(commentId, m, ReactionType.HEART);
            return false;
        } else {
            try {
                reactionRepo.save(CommentReaction.builder()
                        .commentId(commentId)
                        .memberId(m)
                        .type(ReactionType.HEART)
                        .build());
                return true;
            } catch (DataIntegrityViolationException dup) {
                // 동시 요청으로 이미 생긴 경우 → 켜진 상태로 간주
                return true;
            }
        }
    }
}
