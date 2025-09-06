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

    // 목록
    public Slice<Comment> list(Long postId, int page, int size) {
        return commentRepo.findByPostIdAndStatusOrderByCreatedAtAsc(
                postId, CommentStatus.PUBLIC, PageRequest.of(page, size));
    }

    // 댓글 생성
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

    // 조건부 삭제
    @Transactional
    public void delete(Long commentId) {
        Long m = me();
        Comment c = commentRepo.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다. id=" + commentId));
        if (!c.getMemberId().equals(m)) {
            throw new IllegalArgumentException("내가 작성한 댓글만 삭제할 수 있습니다.");
        }

        boolean hasChildren = commentRepo.existsByParentCommentIdAndStatusNot(
                commentId, CommentStatus.DELETED);

        if (hasChildren) {
            commentRepo.softDelete(commentId);
        } else {
            commentRepo.deleteById(commentId);
        }
    }

    // 하트토글 및 like_count 동기화
    @Transactional
    public boolean toggleHeart(Long commentId) {
        Long m = me();
        boolean existed = reactionRepo.existsByCommentIdAndMemberIdAndType(commentId, m, ReactionType.HEART);

        if (existed) {
            reactionRepo.deleteByCommentIdAndMemberIdAndType(commentId, m, ReactionType.HEART);
            syncLikeCount(commentId);
            return false;
        } else {
            try {
                reactionRepo.save(
                        CommentReaction.builder()
                                .commentId(commentId)
                                .memberId(m)
                                .type(ReactionType.HEART)
                                .build()
                );
                syncLikeCount(commentId);
                return true;
            } catch (DataIntegrityViolationException dup) {
                // 거의 동시에 같은 사용자 요청이 들어온 경우 → 이미 만들어졌다고 보고 켜진 상태 반환
                syncLikeCount(commentId);
                return true;
            }
        }
    }

    public long countHearts(Long commentId) {
        return reactionRepo.countByCommentIdAndType(commentId, ReactionType.HEART);
    }

    // 현재 reaction 집계값으로 like_count 세팅
    private void syncLikeCount(Long commentId) {
        int cnt = (int) reactionRepo.countByCommentIdAndType(commentId, ReactionType.HEART);
        commentRepo.updateLikeCount(commentId, cnt);
    }
}