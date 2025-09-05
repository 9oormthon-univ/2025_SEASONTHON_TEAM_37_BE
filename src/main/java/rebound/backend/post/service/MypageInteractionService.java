package rebound.backend.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import rebound.backend.post.entity.CommentStatus;
import rebound.backend.post.entity.ReactionType;
import rebound.backend.post.repository.CommentRepository;
import rebound.backend.post.repository.PostBookmarkRepository;
import rebound.backend.post.repository.PostReactionRepository;
import rebound.backend.utils.InteractionAuth;

@Service
@RequiredArgsConstructor
public class MypageInteractionService {

    private final PostReactionRepository reactionRepo;
    private final PostBookmarkRepository bookmarkRepo;
    private final CommentRepository commentRepo;

    private Long me() { return InteractionAuth.currentMemberId(); }

    public Slice<Long> myLikedPostIds(int page, int size) {
        return reactionRepo.findPostIdsReactedBy(me(), ReactionType.HEART, PageRequest.of(page, size));
    }

    public Slice<Long> myBookmarkedPostIds(int page, int size) {
        return bookmarkRepo.findBookmarkedPostIdsBy(me(), PageRequest.of(page, size));
    }

    public Slice<Long> myCommentedPostIds(int page, int size) {
        return commentRepo.findCommentedPostIds(me(), CommentStatus.PUBLIC, PageRequest.of(page, size));
    }
}
