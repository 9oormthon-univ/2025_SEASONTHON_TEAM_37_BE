package rebound.backend.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import rebound.backend.domain.post.dto.PostCreateRequest;
import rebound.backend.domain.post.dto.PostResponse;
import rebound.backend.domain.post.dto.PostUpdateRequest;
import rebound.backend.domain.post.entity.Post;
import rebound.backend.domain.post.entity.PostContent;
import rebound.backend.domain.post.repository.PostRepository;
import rebound.backend.domain.s3.service.S3Service;
import rebound.backend.domain.tag.entity.Tag;
import rebound.backend.domain.tag.repository.TagRepository;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final S3Service s3Service;

    /**
     * 기능: 새 글을 '발행' 또는 '임시 저장' 상태로 생성
     */
    @Transactional
    public PostResponse createPostWithImage(PostCreateRequest request, MultipartFile file) throws IOException {
        String imageUrl = null;
        // 1. 파일이 존재하는 경우에만 S3에 업로드하고 URL을 가져옵니다.
        if (file != null && !file.isEmpty()) {
            imageUrl = s3Service.uploadFile(file);
        }

        // 2. 게시글 컨텐츠를 생성합니다.
        PostContent postContent = PostContent.builder()
                .situationContent(request.situationContent())
                .failureContent(request.failureContent())
                .learningContent(request.learningContent())
                .nextStepContent(request.nextStepContent())
                .build();

        Post.Status status = (request.publish() != null && request.publish())
                ? Post.Status.PUBLIC
                : Post.Status.DRAFT;

        // 3. 태그를 제외한 Post 엔티티를 먼저 생성합니다.
        Post post = Post.builder()
                .memberId(request.memberId())
                .mainCategory(request.mainCategory())
                .subCategory(request.subCategory())
                .title(request.title())
                .isAnonymous(request.isAnonymous() != null ? request.isAnonymous() : Boolean.FALSE)
                .imageUrl(imageUrl) // S3에서 받은 이미지 URL 저장
                .status(status)
                .build();

        post.setPostContent(postContent);
        postContent.setPost(post);

        // 4. 태그들을 조회하거나 생성한 후, Post에 하나씩 명시적으로 추가합니다.
        if (request.tags() != null && !request.tags().isEmpty()) {
            request.tags().forEach(tagName -> {
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()));
                post.addTag(tag); // 연관관계 편의 메서드 사용
            });
        }

        Post savedPost = postRepository.save(post);

        return PostResponse.from(savedPost);
    }

    @Transactional
    public PostResponse updatePost(Long postId, PostUpdateRequest request, MultipartFile file) throws IOException {
        // TODO: JWT 토큰 등에서 회원 ID를 가져와서, 본인이 쓴 글이 맞는지 확인하는 로직 추가 필요
        Post post = findPostById(postId);

        // 이미지 파일이 새로 첨부된 경우, S3에 업로드하고 URL 업데이트
        if (file != null && !file.isEmpty()) {
            // 참고: 기존 S3 이미지를 삭제하는 로직을 S3Service에 추가하면 더 좋습니다.
            String newImageUrl = s3Service.uploadFile(file);
            post.setImageUrl(newImageUrl);
        }

        // DTO의 내용으로 게시글 필드 업데이트
        post.setMainCategory(request.mainCategory());
        post.setSubCategory(request.subCategory());
        post.setTitle(request.title());
        post.setIsAnonymous(request.isAnonymous());

        PostContent postContent = post.getPostContent();
        postContent.setSituationContent(request.situationContent());
        postContent.setFailureContent(request.failureContent());
        postContent.setLearningContent(request.learningContent());
        postContent.setNextStepContent(request.nextStepContent());

        // 태그 업데이트 (요청된 태그로 전체 교체)
        if (request.tags() != null) {
            // Post 엔티티의 연관관계 편의 메서드를 사용하여 태그를 관리하는 것이 좋습니다.
            post.getTags().clear(); // 이 방식은 orphanRemoval=true 옵션과 함께 사용 시 주의가 필요합니다.
            request.tags().forEach(tagName -> {
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()));
                post.addTag(tag);
            });
        }

        return PostResponse.from(post); // 변경 감지(Dirty Checking)에 의해 자동 업데이트
    }

    @Transactional
    public void deletePost(Long postId) {
        // TODO: JWT 토큰 등에서 회원 ID를 가져와서, 본인이 쓴 글이 맞는지 확인하는 로직 추가 필요
        Post post = findPostById(postId);
        postRepository.delete(post);
    }

    /**
     * 기능: 임시 저장된 글을 '발행' 상태로 변경
     */
    @Transactional
    public void publishPost(Long postId) {
        // TODO: 나중에 JWT 토큰 등에서 회원 ID를 가져와서, 본인이 쓴 글이 맞는지 확인하는 로직 추가하면 좋습니다.
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("ID에 해당하는 게시글을 찾을 수 없습니다: " + postId));

        // 이미 발행된 글은 다시 발행할 수 없도록 방어
        if (post.getStatus() != Post.Status.DRAFT) {
            throw new IllegalStateException("임시 저장 상태의 게시글만 발행할 수 있습니다.");
        }

        // 상태를 PUBLIC으로 변경
        post.setStatus(Post.Status.PUBLIC);
    }

    @Transactional
    public void hidePost(Long postId) {
        // TODO: JWT 토큰 등에서 회원 ID를 가져와서, 본인이 쓴 글이 맞는지 확인하는 로직 추가
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("ID에 해당하는 게시글을 찾을 수 없습니다: " + postId));

        // 공개된 게시글만 '나만 보기'로 변경 가능
        if (post.getStatus() != Post.Status.PUBLIC) {
            throw new IllegalStateException("공개 상태의 게시글만 '나만 보기'로 변경할 수 있습니다.");
        }

        post.setStatus(Post.Status.HIDDEN);
    }

    @Transactional
    public void unhidePost(Long postId) {
        // TODO: JWT 토큰 등에서 회원 ID를 가져와서, 본인이 쓴 글이 맞는지 확인하는 로직 추가
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("ID에 해당하는 게시글을 찾을 수 없습니다: " + postId));

        // '나만 보기' 상태의 게시글만 '전체 공개'로 변경 가능
        if (post.getStatus() != Post.Status.HIDDEN) {
            throw new IllegalStateException("'나만 보기' 상태의 게시글만 '전체 공개'로 변경할 수 있습니다.");
        }

        post.setStatus(Post.Status.PUBLIC);
    }

    public Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("ID에 해당하는 게시글을 찾을 수 없습니다: " + postId));
    }
}
