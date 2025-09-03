package rebound.backend.domain.post.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rebound.backend.domain.post.dto.PostCreateRequest;
import rebound.backend.domain.post.dto.PostResponse;
import rebound.backend.domain.post.service.PostService;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 기능 1: 게시글 생성 (발행 또는 임시 저장)
     */
    @PostMapping
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostCreateRequest request) {
        PostResponse response = postService.createPost(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * 기능 2: 임시 저장된 게시글 발행
     */
    @PatchMapping("/{postId}/publish")
    public ResponseEntity<Void> publishPost(@PathVariable Long postId) {
        postService.publishPost(postId);
        return ResponseEntity.ok().build();
    }
    /**
     * 기능 3: 게시글 '나만 보기' (HIDDEN 상태로 변경)
     */
    @PatchMapping("/{postId}/hide")
    public ResponseEntity<Void> hidePost(@PathVariable Long postId) {
        postService.hidePost(postId);
        return ResponseEntity.ok().build();
    }
    /**
     * 기능 4: '나만 보기' 게시글을 '전체 공개' (PUBLIC 상태로 변경)
     */
    @PatchMapping("/{postId}/unhide")
    public ResponseEntity<Void> unhidePost(@PathVariable Long postId) {
        postService.unhidePost(postId);
        return ResponseEntity.ok().build();
    }
}

