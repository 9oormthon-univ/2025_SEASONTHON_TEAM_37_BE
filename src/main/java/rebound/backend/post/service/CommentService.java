package rebound.backend.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rebound.backend.post.entity.Comment;
import rebound.backend.post.entity.CommentReaction;
import rebound.backend.post.entity.ReactionType;
import rebound.backend.post.repository.CommentReactionRepository;
import rebound.backend.post.repository.CommentRepository;

import java.util.List;

@Service @RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepo;
    private final CommentReactionRepository reactionRepo;

    private Long me() { return rebound.backend.utils.AuthUtils.currentMemberId(); }

    public List<Comment> list(Long postId, int page, int size) {
        return commentRepo.findByPostIdOrderByCreatedAtAsc(postId, PageRequest.of(page, size));
    }

    @Transactional
    public Comment create(Long postId, String content, boolean isAnonymous, Long parentId) {
        var c = Comment.builder()
                .postId(postId).memberId(me())
                .content(content).isAnonymous(isAnonymous)
                .parentCommentId(parentId)
                .build();
        return commentRepo.save(c);
    }

    @Transactional
    public void delete(Long commentId) { commentRepo.deleteById(commentId); }

    @Transactional
    public boolean toggleHeart(Long commentId) {
        Long m = me();
        boolean exists = reactionRepo.existsByCommentIdAndMemberIdAndType(commentId, m, ReactionType.HEART);
        if (exists) reactionRepo.deleteByCommentIdAndMemberIdAndType(commentId, m, ReactionType.HEART);
        else        reactionRepo.save(CommentReaction.builder()
                .commentId(commentId).memberId(m).type(ReactionType.HEART).build());
        // 카운트 컬럼을 별도로 안 두므로, 목록 응답은 프론트에서 합산 요청 or 추가 API로 제공 가능
        return !exists;
    }
}
