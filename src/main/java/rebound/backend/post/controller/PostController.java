package rebound.backend.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import rebound.backend.post.entity.Post;
import rebound.backend.post.service.PostService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시글 목록 조회", description = "다양한 필터링 옵션으로 게시글 목록을 조회합니다. sort: asc(오름차순), desc(내림차순), popular(인기순)")
    @GetMapping
    public ResponseEntity<Page<PostResponse>> getPosts(
            @RequestParam(value = "mainCategory", required = false) MainCategory mainCategory,
            @RequestParam(value = "subCategory", required = false) SubCategory subCategory,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "sort", required = false, defaultValue = "desc") String sort,
            @RequestParam(value = "filter", required = false) String filter,
            @PageableDefault(size = 10) Pageable pageable) {
        
        // 정렬 옵션에 따른 Pageable 조정
        Pageable adjustedPageable = adjustPageableForSort(pageable, sort);
        
        Page<PostResponse> results = postService.getPosts(mainCategory, subCategory, keyword, sort, filter, adjustedPageable);
        return ResponseEntity.ok(results);
    }

    /**
     * 정렬 옵션에 따른 Pageable 조정
     */
    private Pageable adjustPageableForSort(Pageable pageable, String sort) {
        if (sort == null || sort.trim().isEmpty()) {
            return pageable;
        }
        
        switch (sort.toLowerCase()) {
            case "asc":
                return org.springframework.data.domain.PageRequest.of(
                    pageable.getPageNumber(), 
                    pageable.getPageSize(), 
                    org.springframework.data.domain.Sort.by("createdAt").ascending()
                );
            case "desc":
                return org.springframework.data.domain.PageRequest.of(
                    pageable.getPageNumber(), 
                    pageable.getPageSize(), 
                    org.springframework.data.domain.Sort.by("createdAt").descending()
                );
            case "popular":
                // 좋아요 수 기준 정렬 (현재는 생성일 기준으로 대체)
                // TODO: 좋아요 수 기준 정렬을 위해서는 커스텀 쿼리나 @Formula 어노테이션 필요
                return org.springframework.data.domain.PageRequest.of(
                    pageable.getPageNumber(), 
                    pageable.getPageSize(), 
                    org.springframework.data.domain.Sort.by("createdAt").descending()
                );
            default:
                return pageable;
        }
    }

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
            @RequestParam("mainCategory") MainCategory mainCategory,
            @RequestParam("subCategory") SubCategory subCategory,
            @RequestParam("title") String title,
            @RequestParam(value = "isAnonymous", required = false) Boolean isAnonymous,
            @RequestParam(value = "situationContent", required = false) String situationContent,
            @RequestParam(value = "failureContent", required = false) String failureContent,
            @RequestParam(value = "learningContent", required = false) String learningContent,
            @RequestParam(value = "nextStepContent", required = false) String nextStepContent,
            @RequestParam(value = "tags", required = false) List<String> tags,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) throws IOException {

        // 서비스에 전달하기 위해 DTO를 조립합니다.
        PostCreateRequest request = new PostCreateRequest(
                mainCategory,
                subCategory,
                title,
                isAnonymous,
                null,
                situationContent,
                failureContent,
                learningContent,
                nextStepContent,
                tags
        );

        PostResponse response = postService.createPostWithImages(request, files);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * 게시글 수정
     */
    @Operation(summary = "게시글 수정", description = "게시글의 내용, 이미지, 상태 등을 한번에 수정합니다.")
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
            @RequestParam(value = "status", required = false) Post.Status status, // status 파라미터 추가
            @RequestPart(value = "files", required = false) List<MultipartFile> files) throws IOException {

        // DTO 조립 시 status 포함
        PostUpdateRequest request = new PostUpdateRequest(
                mainCategory, subCategory, title, isAnonymous,
                situationContent, failureContent, learningContent, nextStepContent, tags, status
        );

        PostResponse response = postService.updatePost(postId, request, files);
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

}

