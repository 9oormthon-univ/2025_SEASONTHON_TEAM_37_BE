package rebound.backend.post.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import rebound.backend.post.dto.CommentResponse;
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

    @GetMapping("/posts/{postId}/comments")
    public Map<String, Object> list(@PathVariable @Positive Long postId,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        // 서비스로부터 모든 정보가 담긴 CommentResponse DTO 슬라이스를 받습니다.
        Slice<CommentResponse> slice = service.list(postId, page, size);

        // 더 이상 컨트롤러에서 추가적인 service 호출(N+1 유발)을 할 필요가 없습니다.
        return Map.of(
                "items", slice.getContent(), // DTO 리스트를 그대로 사용
                "hasNext", slice.hasNext(),
                "page", page
        );
    }

    /** 생성 */
    @PostMapping("/posts/{postId}/comments")
    public CommentDto create(@PathVariable @Positive Long postId, @Valid @RequestBody CreateReq req) {
        Comment created = service.create(postId, req.getContent(), req.isAnonymous(), req.getParentCommentId());
        return CommentDto.from(created, 0L); // 막 생성했으니 0으로 응답(원하면 countHearts로 재계산 가능)
    }

    /** 삭제(조건부 소프트/하드) */
    @DeleteMapping("/comments/{id}")
    public void delete(@PathVariable @Positive Long id) {
        service.delete(id);
    }

    /** 하트 토글 */
    @PostMapping("/comments/{id}/reactions/heart")
    public Map<String, Object> toggleHeart(@PathVariable @Positive Long id) {
        boolean liked = service.toggleHeart(id);
        long likeCount = service.countHearts(id); // 토글 후 최신 카운트 내려주면 프론트 반영 쉬움
        return Map.of("liked", liked, "likeCount", likeCount);
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
        private long likeCount;                 // ← int → long (집계 결과 타입과 맞춤)
        private String createdAt;

        /** 기존 from(Comment) 대신 "읽을 때 집계한 likeCount"를 받아서 셋팅 */
        public static CommentDto from(Comment c, long likeCount) {
            CommentDto d = new CommentDto();
            d.commentId = c.getCommentId();
            d.postId = c.getPostId();
            d.memberId = c.getMemberId();
            d.parentCommentId = c.getParentCommentId();
            d.content = c.getContent();
            d.anonymous = c.isAnonymous();
            CommentStatus st = c.getStatus();
            d.status = (st == null) ? null : st.name();
            d.likeCount = likeCount;
            d.createdAt = (c.getCreatedAt() == null)
                    ? null
                    : DateTimeFormatter.ISO_INSTANT.format(c.getCreatedAt());
            return d;
        }
    }
}
