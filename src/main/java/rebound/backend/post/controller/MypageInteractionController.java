package rebound.backend.post.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import rebound.backend.post.dto.MyInteractionStateDto;
import rebound.backend.post.dto.SliceResponse;
import rebound.backend.post.entity.ReactionType;
import rebound.backend.post.repository.PostBookmarkRepository;
import rebound.backend.post.repository.PostReactionRepository;
import rebound.backend.post.service.MypageInteractionService;
import rebound.backend.utils.InteractionAuth;

@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
public class MypageInteractionController {

    private final MypageInteractionService service;
    private final PostReactionRepository reactionRepo;
    private final PostBookmarkRepository bookmarkRepo;

    // 좋아요한 글 목록 (postId만 반환)
    @GetMapping("/likes")
    public SliceResponse<Long> myLikedPosts(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        return SliceResponse.of(service.myLikedPostIds(page, size));
    }

    // 스크랩한 글 목록 (postId만 반환)
    @GetMapping("/bookmarks")
    public SliceResponse<Long> myBookmarkedPosts(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        return SliceResponse.of(service.myBookmarkedPostIds(page, size));
    }

    // 내가 댓글 단 글 목록 (postId만 반환)
    @GetMapping("/comments")
    public SliceResponse<Long> myCommentedPosts(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        return SliceResponse.of(service.myCommentedPostIds(page, size));
    }

    // (카드에서 사용) 단일 게시글에 대한 '내 상호작용 상태' 조회
    @GetMapping("/posts/{postId}/my-interactions")
    public MyInteractionStateDto myInteractionState(@PathVariable Long postId) {
        Long me = InteractionAuth.currentMemberId();
        boolean liked = reactionRepo.existsByPostIdAndMemberIdAndType(postId, me, ReactionType.HEART);
        boolean bookmarked = bookmarkRepo.existsByPostIdAndMemberId(postId, me);
        long likeCount = reactionRepo.countByPostIdAndType(postId, ReactionType.HEART);
        long bookmarkCount = bookmarkRepo.countByPostId(postId);
        return new MyInteractionStateDto(liked, bookmarked, likeCount, bookmarkCount);
    }
}
