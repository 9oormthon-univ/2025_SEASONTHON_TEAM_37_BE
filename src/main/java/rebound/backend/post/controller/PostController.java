package rebound.backend.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rebound.backend.category.entity.MainCategory;
import rebound.backend.category.entity.SubCategory;
import rebound.backend.post.dto.PostCreateRequest;
import rebound.backend.post.dto.PostResponse;
import rebound.backend.post.dto.PostUpdateRequest;
import rebound.backend.post.service.PostService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시글 상세 조회")
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPostDetails(@PathVariable Long postId) {
        PostResponse response = postService.getPostDetails(postId);
        return ResponseEntity.ok(response);
    }

    /**
     * 기능 1: 게시글 생성 (발행 또는 임시 저장)
     */
    @Operation(summary = "게시글 생성 (이미지 포함)", description = "게시글 데이터와 이미지 파일을 multipart/form-data 형식으로 받습니다.")
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<PostResponse> createPost(
            @RequestParam("memberId") Long memberId,
            @RequestParam("mainCategory") MainCategory mainCategory,
            @RequestParam("subCategory") SubCategory subCategory,
            @RequestParam("title") String title,
            @RequestParam(value = "isAnonymous", required = false) Boolean isAnonymous,
            @RequestParam(value = "situationContent", required = false) String situationContent,
            @RequestParam(value = "failureContent", required = false) String failureContent,
            @RequestParam(value = "learningContent", required = false) String learningContent,
            @RequestParam(value = "nextStepContent", required = false) String nextStepContent,
            @RequestParam(value = "publish", required = false) Boolean publish,
            @RequestParam(value = "tags", required = false) List<String> tags,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        // 서비스에 전달하기 위해 DTO를 조립합니다.
        PostCreateRequest request = new PostCreateRequest(
                memberId,
                mainCategory,
                subCategory,
                title,
                isAnonymous,
                null,
                situationContent,
                failureContent,
                learningContent,
                nextStepContent,
                publish,
                tags
        );

        PostResponse response = postService.createPostWithImage(request, file);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * 게시글 수정
     */
    @Operation(summary = "게시글 수정", description = "게시글의 내용과 이미지를 수정합니다.")
    @PatchMapping(value = "/{postId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long postId,
            @RequestParam("mainCategory") MainCategory mainCategory,
            @RequestParam("subCategory") SubCategory subCategory,
            @RequestParam("title") String title,
            @RequestParam("isAnonymous") Boolean isAnonymous,
            @RequestParam(value = "situationContent", required = false) String situationContent,
            @RequestParam(value = "failureContent", required = false) String failureContent,
            @RequestParam(value = "learningContent", required = false) String learningContent,
            @RequestParam(value = "nextStepContent", required = false) String nextStepContent,
            @RequestParam(value = "tags", required = false) List<String> tags,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        PostUpdateRequest request = new PostUpdateRequest(
                mainCategory, subCategory, title, isAnonymous,
                situationContent, failureContent, learningContent, nextStepContent, tags
        );

        PostResponse response = postService.updatePost(postId, request, file);
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

