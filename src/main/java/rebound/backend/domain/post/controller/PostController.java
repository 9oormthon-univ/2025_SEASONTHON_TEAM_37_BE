package rebound.backend.domain.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rebound.backend.domain.category.entity.MainCategory;
import rebound.backend.domain.category.entity.SubCategory;
import rebound.backend.domain.post.dto.PostCreateRequest;
import rebound.backend.domain.post.dto.PostResponse;
import rebound.backend.domain.post.service.PostService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 기능 1: 게시글 생성 (발행 또는 임시 저장)
     */
    @Operation(summary = "게시글 생성 (이미지 포함)", description = "게시글 데이터와 이미지 파일을 multipart/form-data 형식으로 받습니다.")
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<PostResponse> createPost(
            // ✅ 제안해주신 대로, 각 필드를 @RequestParam으로 받도록 수정했습니다.
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

