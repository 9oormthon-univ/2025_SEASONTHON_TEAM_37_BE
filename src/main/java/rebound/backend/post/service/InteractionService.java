package rebound.backend.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rebound.backend.post.entity.PostBookmark;
import rebound.backend.post.entity.PostReaction;
import rebound.backend.post.entity.ReactionType;
import rebound.backend.post.repository.PostBookmarkRepository;
import rebound.backend.post.repository.PostReactionRepository;

@Service @RequiredArgsConstructor
public class InteractionService {
    private final PostReactionRepository reactionRepo;
    private final PostBookmarkRepository bookmarkRepo;

    // TODO: OAuth 붙으면 SecurityContext에서 memberId 가져오기
    private Long me() { return rebound.backend.utils.AuthUtils.currentMemberId(); }

    @Transactional
    public ToggleResult toggleHeart(Long postId) {
        Long m = me();
        boolean exists = reactionRepo.existsByPostIdAndMemberIdAndType(postId, m, ReactionType.HEART);
        boolean liked = !exists;
        if (exists) reactionRepo.deleteByPostIdAndMemberIdAndType(postId, m, ReactionType.HEART);
        else        reactionRepo.save(PostReaction.builder().postId(postId).memberId(m).type(ReactionType.HEART).build());
        long count = reactionRepo.countByPostIdAndType(postId, ReactionType.HEART);
        return new ToggleResult(liked, count);
    }

    @Transactional
    public ToggleResult toggleBookmark(Long postId) {
        Long m = me();
        boolean exists = bookmarkRepo.existsByPostIdAndMemberId(postId, m);
        boolean bookmarked = !exists;
        if (exists) bookmarkRepo.deleteByPostIdAndMemberId(postId, m);
        else        bookmarkRepo.save(PostBookmark.builder().postId(postId).memberId(m).build());
        long count = bookmarkRepo.countByPostId(postId);
        return new ToggleResult(bookmarked, count);
    }

    public record ToggleResult(boolean state, long count) {}
}
