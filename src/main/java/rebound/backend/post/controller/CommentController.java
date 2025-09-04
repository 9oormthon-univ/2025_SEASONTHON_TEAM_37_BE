package rebound.backend.post.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import rebound.backend.post.entity.Comment;
import rebound.backend.post.service.CommentService;

import java.util.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService service;

    @GetMapping("/posts/{postId}/comments")
    public Map<String,Object> list(@PathVariable Long postId,
                                   @RequestParam(defaultValue="0") int page,
                                   @RequestParam(defaultValue="20") int size) {
        var items = service.list(postId, page, size).stream().map(CommentDto::from).toList();
        return Map.of("items", items);
    }

    @PostMapping("/posts/{postId}/comments")
    public CommentDto create(@PathVariable Long postId, @RequestBody CreateReq req) {
        return CommentDto.from(service.create(postId, req.content, req.isAnonymous, req.parentCommentId));
    }

    @DeleteMapping("/comments/{id}")
    public void delete(@PathVariable Long id) { service.delete(id); }

    @PostMapping("/comments/{id}/reactions/heart")
    public Map<String, Object> toggleHeart(@PathVariable Long id) {
        boolean liked = service.toggleHeart(id);
        return Map.of("liked", liked);
    }

    @Data static class CreateReq { String content; boolean isAnonymous = true; Long parentCommentId; }

    @Data static class CommentDto {
        Long commentId; Long postId; Long memberId; Long parentCommentId;
        String content; boolean anonymous; String status; int likeCount; String createdAt;

        static CommentDto from(Comment c) {
            CommentDto d = new CommentDto();
            d.commentId = c.getCommentId();
            d.postId = c.getPostId();
            d.memberId = c.getMemberId();
            d.parentCommentId = c.getParentCommentId();
            d.content = c.getContent();
            d.anonymous = c.isAnonymous();
            d.status = c.getStatus();
            d.likeCount = c.getLikeCount();
            d.createdAt = c.getCreatedAt() == null ? null : c.getCreatedAt().toString();
            return d;
        }
    }
}
