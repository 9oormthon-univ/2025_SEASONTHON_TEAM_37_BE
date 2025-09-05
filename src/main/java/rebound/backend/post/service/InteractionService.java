package rebound.backend.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rebound.backend.post.entity.PostBookmark;
import rebound.backend.post.entity.PostReaction;
import rebound.backend.post.entity.ReactionType;
import rebound.backend.post.repository.PostBookmarkRepository;
import rebound.backend.post.repository.PostReactionRepository;
import rebound.backend.utils.InteractionAuth;

@Service @RequiredArgsConstructor
public class InteractionService {
    private final PostReactionRepository reactionRepo;
    private final PostBookmarkRepository bookmarkRepo;

    // TODO: OAuth 붙으면 SecurityContext에서 memberId 가져오기
    private Long me() { return InteractionAuth.currentMemberId(); }

    @Transactional
    public ToggleResult toggleHeart(Long postId) {
        Long m = me();
        boolean exists = reactionRepo.existsByPostIdAndMemberIdAndType(postId, m, ReactionType.HEART);
        boolean liked;
        if (exists) {
            reactionRepo.deleteByPostIdAndMemberIdAndType(postId, m, ReactionType.HEART);
            liked = false;
        } else {
            try {
                reactionRepo.save(PostReaction.builder()
                        .postId(postId).memberId(m).type(ReactionType.HEART).build());
                liked = true;
            } catch (DataIntegrityViolationException dup) {
                liked = true; // 경합 시 이미 켜진 상태로 간주
            }
        }
        long count = reactionRepo.countByPostIdAndType(postId, ReactionType.HEART);
        return new ToggleResult(liked, count);
    }

    @Transactional
    public ToggleResult toggleBookmark(Long postId) {
        Long m = me();
        boolean exists = bookmarkRepo.existsByPostIdAndMemberId(postId, m);
        boolean bookmarked;
        if (exists) {
            bookmarkRepo.deleteByPostIdAndMemberId(postId, m);
            bookmarked = false;
        } else {
            try {
                bookmarkRepo.save(PostBookmark.builder().postId(postId).memberId(m).build());
                bookmarked = true;
            } catch (DataIntegrityViolationException dup) {
                bookmarked = true;
            }
        }
        long count = bookmarkRepo.countByPostId(postId);
        return new ToggleResult(bookmarked, count);
    }

    public record ToggleResult(boolean state, long count) {}
}