package rebound.backend.post.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import rebound.backend.post.service.InteractionService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class InteractionController {
    private final InteractionService service;

    @PostMapping("/posts/{postId}/reactions/heart")
    public Map<String, Object> toggleHeart(@PathVariable Long postId) {
        var r = service.toggleHeart(postId);
        return Map.of("liked", r.state(), "likeCount", r.count());
    }

    @PostMapping("/posts/{postId}/bookmarks")
    public Map<String, Object> toggleBookmark(@PathVariable Long postId) {
        var r = service.toggleBookmark(postId);
        return Map.of("bookmarked", r.state(), "bookmarkCount", r.count());
    }
}
