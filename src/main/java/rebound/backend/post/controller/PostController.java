package rebound.backend.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rebound.backend.category.entity.MainCategory;
import rebound.backend.category.entity.SubCategory;
import rebound.backend.post.dto.PostCreateRequest;
import rebound.backend.post.dto.PostResponse;
import rebound.backend.post.dto.PostUpdateRequest;
import rebound.backend.post.service.PostService;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "카테고리별 게시글 목록 조회", description = "소분류(subCategory)를 기준으로 게시글 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<Page<PostResponse>> getPosts(
            @RequestParam(value = "mainCategory", required = false) MainCategory mainCategory,
            @RequestParam(value = "subCategory") SubCategory subCategory,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<PostResponse> results = postService.getPosts(mainCategory, subCategory, pageable);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "최신 게시글 목록 조회", description = "최신순으로 정렬된 게시글 목록을 조회합니다.")
    @GetMapping("/recent")
    public ResponseEntity<Page<PostResponse>> getRecentPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PostResponse> results = postService.getRecentPosts(pageable);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "게시글 상세 조회")
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPostDetails(@PathVariable Long postId) {
        PostResponse response = postService.getPostDetails(postId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내가 쓴 글 목록 조회", description = "현재 로그인한 사용자가 작성한 글 목록을 최신순으로 조회합니다.")
    @GetMapping("/my")
    public ResponseEntity<Page<PostResponse>> getMyPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PostResponse> results = postService.getMyPosts(pageable);
        return ResponseEntity.ok(results);
    }

    /**
     * 기능 1: 게시글 생성 (발행 또는 임시 저장)
     */
    @Operation(summary = "게시글 생성 (이미지 포함)", description = "게시글 데이터를 JSON 형식으로 받습니다.")
    @PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<PostResponse> createPost(@RequestBody PostCreateRequest request) throws IOException {
        PostResponse response = postService.createPostWithImages(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * 게시글 수정
     */
    @Operation(summary = "게시글 수정", description = "게시글의 내용, 이미지, 상태 등을 한번에 수정합니다.")
    @PatchMapping(value = "/{postId}", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long postId,
            @RequestBody PostUpdateRequest request) throws IOException {
        PostResponse response = postService.updatePost(postId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 게시글 삭제
     */
    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return ResponseEntity.noContent().build(); // 204 No Content 응답
    }

    @Operation(summary = "게시글 검색", description = "키워드를 사용하여 제목, 내용, 태그에서 게시글을 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<Page<PostResponse>> searchPosts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<PostResponse> results = postService.searchPostsByKeyword(keyword, pageable);
        return ResponseEntity.ok(results);
    }
}
