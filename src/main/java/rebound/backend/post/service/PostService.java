package rebound.backend.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import rebound.backend.category.entity.MainCategory;
import rebound.backend.category.entity.SubCategory;
import rebound.backend.member.domain.Member;
import rebound.backend.member.repository.MemberRepository;
import rebound.backend.post.dto.PostCreateRequest;
import rebound.backend.post.dto.PostResponse;
import rebound.backend.post.dto.PostUpdateRequest;
import rebound.backend.post.entity.Post;
import rebound.backend.post.entity.PostContent;
import rebound.backend.post.entity.PostImage;
import rebound.backend.post.repository.PostRepository;
import rebound.backend.post.repository.PostSpecification;
import rebound.backend.s3.service.S3Service;
import rebound.backend.tag.entity.Tag;
import rebound.backend.tag.repository.TagRepository;
import rebound.backend.utils.NicknameMasker;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final S3Service s3Service;
    private final MemberRepository memberRepository;

    /**
     * 카테고리별 게시글 목록 조회
     */
    public Page<PostResponse> getPosts(MainCategory mainCategory, SubCategory subCategory, Pageable pageable) {
        if (subCategory == null) {
            return Page.empty(pageable);
        }

        Specification<Post> spec = PostSpecification.hasSubCategory(subCategory);

        if (mainCategory != null) {
            spec = spec.and(PostSpecification.hasMainCategory(mainCategory));
        }

        Page<Post> posts = postRepository.findAll(spec, pageable);
        return mapToPostResponsePage(posts);
    }


    /**
     * 최신 게시글 목록 조회
     */
    public Page<PostResponse> getRecentPosts(Pageable pageable) {
        Page<Post> posts = postRepository.findAll(pageable);
        return mapToPostResponsePage(posts);
    }

    /**
     * 게시글 상세 조회
     */
    public PostResponse getPostDetails(Long postId) {
        Post post = findPostById(postId);
        Member author = memberRepository.findById(post.getMemberId())
                .orElse(null); // 탈퇴한 회원의 경우를 대비해 null 처리
        return PostResponse.from(post, author);
    }

    /**
     * 게시글 생성 (여러 이미지 포함)
     */
    @Transactional
    public PostResponse createPostWithImages(PostCreateRequest request, List<MultipartFile> files) throws IOException {
        Long currentMemberId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        Member currentMember = memberRepository.findById(currentMemberId)
                .orElseThrow(() -> new IllegalArgumentException("현재 로그인된 사용자 정보를 찾을 수 없습니다."));

        PostContent postContent = PostContent.builder()
                .situationContent(request.situationContent()).failureContent(request.failureContent())
                .learningContent(request.learningContent()).nextStepContent(request.nextStepContent()).build();

        Post post = Post.builder()
                .memberId(currentMember.getId())
                .mainCategory(request.mainCategory()).subCategory(request.subCategory())
                .title(request.title()).isAnonymous(request.isAnonymous() != null ? request.isAnonymous() : Boolean.FALSE)
                .build();
        post.setPostContent(postContent);
        postContent.setPost(post);

        if (files != null && !files.isEmpty()) {
            List<String> imageUrls = s3Service.uploadFiles(files);
            for (int i = 0; i < imageUrls.size(); i++) {
                post.addImage(PostImage.builder().imageUrl(imageUrls.get(i)).imageOrder(i).build());
            }
        }
        if (request.tags() != null && !request.tags().isEmpty()) {
            request.tags().forEach(tagName -> post.addTag(tagRepository.findByName(tagName)
                    .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()))));
        }
        Post savedPost = postRepository.save(post);
        return PostResponse.from(savedPost, currentMember);
    }


    /**
     * 내가 쓴 글 목록 조회
     */
    public Page<PostResponse> getMyPosts(Pageable pageable) {
        Long currentMemberId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());

        Specification<Post> spec = PostSpecification.hasMemberId(currentMemberId);
        Page<Post> posts = postRepository.findAll(spec, pageable);

        return mapToPostResponsePage(posts);
    }

    /**
     * 게시글 수정
     */
    @Transactional
    public PostResponse updatePost(Long postId, PostUpdateRequest request, List<MultipartFile> files) throws IOException {
        Post post = findPostById(postId);
        authorizePostAuthor(post);

        post.getPostImages().clear();
        if (files != null && !files.isEmpty()) {
            List<String> imageUrls = s3Service.uploadFiles(files);
            for (int i = 0; i < imageUrls.size(); i++) {
                post.addImage(PostImage.builder().imageUrl(imageUrls.get(i)).imageOrder(i).build());
            }
        }
        post.setMainCategory(request.mainCategory());
        post.setSubCategory(request.subCategory());
        post.setTitle(request.title());
        post.setIsAnonymous(request.isAnonymous());
        PostContent postContent = post.getPostContent();
        postContent.setSituationContent(request.situationContent());
        postContent.setFailureContent(request.failureContent());
        postContent.setLearningContent(request.learningContent());
        postContent.setNextStepContent(request.nextStepContent());
        if (request.tags() != null) {
            post.getTags().clear();
            request.tags().forEach(tagName -> post.addTag(tagRepository.findByName(tagName)
                    .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()))));
        }
        if (request.status() != null) {
            post.setStatus(request.status());
        }
        Member author = memberRepository.findById(post.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("작성자 정보를 찾을 수 없습니다."));
        return PostResponse.from(post, author);
    }



    /**
     * 게시글 검색 (키워드 기반, 페이징 포함) - PostResponse DTO 사용
     */
    public Page<PostResponse> searchPostsByKeyword(String keyword, Pageable pageable) {
        Specification<Post> spec = PostSpecification.searchByKeyword(keyword);
        Page<Post> posts = postRepository.findAll(spec, pageable);
        return mapToPostResponsePage(posts);
    }

    @Transactional
    public void deletePost(Long postId) {
        Post post = findPostById(postId);
        authorizePostAuthor(post);
        postRepository.delete(post);
    }


    private void authorizePostAuthor(Post post) {
        Long currentMemberId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        if (!post.getMemberId().equals(currentMemberId)) {
            throw new AccessDeniedException("해당 게시글에 대한 권한이 없습니다.");
        }
    }

    public Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("ID에 해당하는 게시글을 찾을 수 없습니다: " + postId));
    }

    private Page<PostResponse> mapToPostResponsePage(Page<Post> posts) {
        List<Long> authorIds = posts.getContent().stream()
                .map(Post::getMemberId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Member> authors = memberRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(Member::getId, member -> member));

        return posts.map(post -> {
            Member author = authors.get(post.getMemberId());
            return PostResponse.from(post, author);
        });
    }
}
