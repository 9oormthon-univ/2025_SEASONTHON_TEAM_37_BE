// path: src/main/java/rebound/backend/post/controller/CommentController.java
package rebound.backend.post.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import rebound.backend.post.entity.Comment;
import rebound.backend.post.entity.CommentStatus;
import rebound.backend.post.service.CommentService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class CommentController {

    private final CommentService service;

    /** 무한스크롤: items + hasNext + page */
    @GetMapping("/posts/{postId}/comments")
    public Map<String, Object> list(@PathVariable @Positive Long postId,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        Slice<Comment> slice = service.list(postId, page, size);
        List<CommentDto> items = slice.getContent().stream().map(CommentDto::from).toList();
        return Map.of(
                "items", items,
                "hasNext", slice.hasNext(),
                "page", page
        );
    }

    @PostMapping("/posts/{postId}/comments")
    public CommentDto create(@PathVariable @Positive Long postId, @Valid @RequestBody CreateReq req) {
        return CommentDto.from(service.create(postId, req.getContent(), req.isAnonymous(), req.getParentCommentId()));
    }

    @DeleteMapping("/comments/{id}")
    public void delete(@PathVariable @Positive Long id) {
        service.delete(id);
    }

    @PostMapping("/comments/{id}/reactions/heart")
    public Map<String, Object> toggleHeart(@PathVariable @Positive Long id) {
        boolean liked = service.toggleHeart(id);
        return Map.of("liked", liked);
    }

    // ===== DTO =====

    @Data
    public static class CreateReq {
        @NotBlank(message = "내용은 비어 있을 수 없습니다.")
        private String content;
        private boolean isAnonymous = true;
        private Long parentCommentId;
    }

    @Data
    public static class CommentDto {
        private Long commentId;
        private Long postId;
        private Long memberId;
        private Long parentCommentId;
        private String content;
        private boolean anonymous;
        private String status;
        private int likeCount;
        private String createdAt;

        public static CommentDto from(Comment c) {
            CommentDto d = new CommentDto();
            d.commentId = c.getCommentId();
            d.postId = c.getPostId();
            d.memberId = c.getMemberId();
            d.parentCommentId = c.getParentCommentId();
            d.content = c.getContent();
            d.anonymous = c.isAnonymous();
            // enum → 문자열로 안전 변환
            CommentStatus st = c.getStatus();
            d.status = (st == null) ? null : st.name();
            d.likeCount = c.getLikeCount();
            d.createdAt = (c.getCreatedAt() == null)
                    ? null
                    : DateTimeFormatter.ISO_INSTANT.format(c.getCreatedAt());
            return d;
        }
    }
}
