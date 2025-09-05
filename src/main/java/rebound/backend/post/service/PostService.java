package rebound.backend.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import rebound.backend.member.domain.Member;
import rebound.backend.member.repository.MemberRepository;
import rebound.backend.post.dto.PostCreateRequest;
import rebound.backend.post.dto.PostResponse;
import rebound.backend.post.dto.PostUpdateRequest;
import rebound.backend.post.entity.Post;
import rebound.backend.post.entity.PostContent;
import rebound.backend.post.repository.PostRepository;
import rebound.backend.s3.service.S3Service;
import rebound.backend.tag.entity.Tag;
import rebound.backend.tag.repository.TagRepository;
import rebound.backend.utils.NicknameMasker;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final S3Service s3Service;
    private final MemberRepository memberRepository;

    /**
     * 게시글 상세 조회 (익명 닉네임 처리 기능 포함)
     */
    public PostResponse getPostDetails(Long postId) {
        Post post = findPostById(postId);

        // memberId로 작성자 Member 엔티티를 조회
        Member author = memberRepository.findById(post.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("작성자 정보를 찾을 수 없습니다."));

        String finalNickname;
        if (post.getIsAnonymous()) {
            // 익명일 경우, NicknameMasker를 사용해 닉네임을 마스킹합니다.
            finalNickname = NicknameMasker.mask(author.getNickname());
        } else {
            // 익명이 아닐 경우, 원래 닉네임을 그대로 사용합니다.
            finalNickname = author.getNickname();
        }

        // 최종적으로 가공된 닉네임을 DTO에 담아 반환합니다.
        return PostResponse.from(post, finalNickname);
    }

    /**
     * 게시글 생성 (이미지 포함)
     */
    @Transactional
    public PostResponse createPostWithImage(PostCreateRequest request, MultipartFile file) throws IOException {
        // SecurityContextHolder에서 현재 로그인한 사용자의 정보를 가져옴
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        Member currentMember = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("현재 로그인된 사용자 정보를 찾을 수 없습니다."));

        String imageUrl = null;
        if (file != null && !file.isEmpty()) {
            imageUrl = s3Service.uploadFile(file);
        }

        PostContent postContent = PostContent.builder()
                .situationContent(request.situationContent())
                .failureContent(request.failureContent())
                .learningContent(request.learningContent())
                .nextStepContent(request.nextStepContent())
                .build();

        Post.Status status = (request.publish() != null && request.publish())
                ? Post.Status.PUBLIC
                : Post.Status.DRAFT;

        Post post = Post.builder()
                .memberId(currentMember.getId()) // JWT 토큰에서 조회한 사용자 ID 사용
                .mainCategory(request.mainCategory())
                .subCategory(request.subCategory())
                .title(request.title())
                .isAnonymous(request.isAnonymous() != null ? request.isAnonymous() : Boolean.FALSE)
                .imageUrl(imageUrl)
                .status(status)
                .build();

        post.setPostContent(postContent);
        postContent.setPost(post);

        if (request.tags() != null && !request.tags().isEmpty()) {
            request.tags().forEach(tagName -> {
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()));
                post.addTag(tag);
            });
        }

        Post savedPost = postRepository.save(post);

        String finalNickname = savedPost.getIsAnonymous()
                ? NicknameMasker.mask(currentMember.getNickname())
                : currentMember.getNickname();

        return PostResponse.from(savedPost, finalNickname);
    }

    @Transactional
    public PostResponse updatePost(Long postId, PostUpdateRequest request, MultipartFile file) throws IOException {
        Post post = findPostById(postId);
        authorizePostAuthor(post);
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

        Member author = memberRepository.findById(post.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("작성자 정보를 찾을 수 없습니다."));

        // 익명 여부에 따라 닉네임을 가공합니다.
        String finalNickname = post.getIsAnonymous()
                ? NicknameMasker.mask(author.getNickname())
                : author.getNickname();

        // 최종적으로 모든 정보(닉네임, 카테고리 포함)를 담아 응답을 생성합니다.
        return PostResponse.from(post, finalNickname);
    }

    @Transactional
    public void deletePost(Long postId) {
        Post post = findPostById(postId);
        authorizePostAuthor(post);
        postRepository.delete(post);
    }

    /**
     * 기능: 임시 저장된 글을 '발행' 상태로 변경
     */
    @Transactional
    public void publishPost(Long postId) {
        Post post = findPostById(postId);
        authorizePostAuthor(post);

        // 이미 발행된 글은 다시 발행할 수 없도록 방어
        if (post.getStatus() != Post.Status.DRAFT) {
            throw new IllegalStateException("임시 저장 상태의 게시글만 발행할 수 있습니다.");
        }

        // 상태를 PUBLIC으로 변경
        post.setStatus(Post.Status.PUBLIC);
    }

    @Transactional
    public void hidePost(Long postId) {
        Post post = findPostById(postId);
        authorizePostAuthor(post);

        // 공개된 게시글만 '나만 보기'로 변경 가능
        if (post.getStatus() != Post.Status.PUBLIC) {
            throw new IllegalStateException("공개 상태의 게시글만 '나만 보기'로 변경할 수 있습니다.");
        }

        post.setStatus(Post.Status.HIDDEN);
    }

    @Transactional
    public void unhidePost(Long postId) {
        Post post = findPostById(postId);
        authorizePostAuthor(post);

        // '나만 보기' 상태의 게시글만 '전체 공개'로 변경 가능
        if (post.getStatus() != Post.Status.HIDDEN) {
            throw new IllegalStateException("'나만 보기' 상태의 게시글만 '전체 공개'로 변경할 수 있습니다.");
        }

        post.setStatus(Post.Status.PUBLIC);
    }

    private void authorizePostAuthor(Post post) {
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        Member currentMember = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("현재 로그인된 사용자 정보를 찾을 수 없습니다."));

        if (!post.getMemberId().equals(currentMember.getId())) {
            throw new AccessDeniedException("해당 게시글에 대한 수정/삭제 권한이 없습니다.");
        }
    }

    public Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("ID에 해당하는 게시글을 찾을 수 없습니다: " + postId));
    }
}
