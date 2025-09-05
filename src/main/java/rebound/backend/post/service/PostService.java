package rebound.backend.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import rebound.backend.post.entity.ReactionType;
import rebound.backend.post.repository.PostBookmarkRepository;
import rebound.backend.post.repository.PostReactionRepository;
import rebound.backend.post.repository.PostRepository;
import rebound.backend.post.repository.PostSpecification;
import rebound.backend.tag.entity.Tag;
import rebound.backend.tag.repository.TagRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final MemberRepository memberRepository;
    private final PostReactionRepository postReactionRepository;
    private final PostBookmarkRepository postBookmarkRepository;

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
                .orElse(null);

        long likeCount = postReactionRepository.countByPostIdAndType(postId, ReactionType.HEART);
        long bookmarkCount = postBookmarkRepository.countByPostId(postId);

        Long me = currentMemberIdOrNull();
        boolean liked = false, bookmarked = false;
        if (me != null) {
            liked = postReactionRepository.existsByPostIdAndMemberIdAndType(postId, me, ReactionType.HEART);
            bookmarked = postBookmarkRepository.existsByPostIdAndMemberId(postId, me);
        }

        return PostResponse.from(post, author, likeCount, bookmarkCount, liked, bookmarked);
    }

    /**
     * 게시글 생성 (JSON 기반)
     */
    @Transactional
    public PostResponse createPost(PostCreateRequest request) {
        Long currentMemberId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        Member currentMember = memberRepository.findById(currentMemberId)
                .orElseThrow(() -> new IllegalArgumentException("현재 로그인된 사용자 정보를 찾을 수 없습니다."));

        PostContent postContent = PostContent.builder()
                .situationContent(request.situationContent())
                .failureContent(request.failureContent())
                .learningContent(request.learningContent())
                .nextStepContent(request.nextStepContent())
                .build();

        Post post = Post.builder()
                .memberId(currentMember.getId())
                .mainCategory(request.mainCategory())
                .subCategory(request.subCategory())
                .title(request.title())
                .isAnonymous(request.isAnonymous() != null ? request.isAnonymous() : Boolean.FALSE)
                .build();

        post.setPostContent(postContent);
        postContent.setPost(post);

        if (request.postImages() != null && !request.postImages().isEmpty()) {
            for (int i = 0; i < request.postImages().size(); i++) {
                String imageUrl = request.postImages().get(i).imageUrl();
                PostImage postImage = PostImage.builder()
                        .imageUrl(imageUrl)
                        .imageOrder(i)
                        .build();
                post.addImage(postImage);
            }
        }

        if (request.tags() != null && !request.tags().isEmpty()) {
            request.tags().forEach(tagName -> post.addTag(tagRepository.findByName(tagName)
                    .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()))));
        }
        Post savedPost = postRepository.save(post);

        return PostResponse.from(savedPost, currentMember, 0L, 0L, false, false);
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
     * 게시글 수정 (JSON 기반)
     */
    @Transactional
    public PostResponse updatePost(Long postId, PostUpdateRequest request) {
        Post post = findPostById(postId);
        authorizePostAuthor(post);

        post.getPostImages().clear();
        if (request.postImages() != null && !request.postImages().isEmpty()) {
            for (int i = 0; i < request.postImages().size(); i++) {
                String imageUrl = request.postImages().get(i).imageUrl();
                PostImage postImage = PostImage.builder()
                        .imageUrl(imageUrl)
                        .imageOrder(i)
                        .build();
                post.addImage(postImage);
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

        long likeCount = postReactionRepository.countByPostIdAndType(postId, ReactionType.HEART);
        long bookmarkCount = postBookmarkRepository.countByPostId(postId);
        Long me = currentMemberIdOrNull();
        boolean liked = false, bookmarked = false;
        if (me != null) {
            liked = postReactionRepository.existsByPostIdAndMemberIdAndType(postId, me, ReactionType.HEART);
            bookmarked = postBookmarkRepository.existsByPostIdAndMemberId(postId, me);
        }
        return PostResponse.from(post, author, likeCount, bookmarkCount, liked, bookmarked);
    }

    /**
     * 게시글 검색 (키워드 기반, 페이징 포함)
     */
    public Page<PostResponse> searchPostsByKeyword(String keyword, Pageable pageable) {
        Specification<Post> spec = PostSpecification.searchByKeyword(keyword);
        Page<Post> posts = postRepository.findAll(spec, pageable);
        return mapToPostResponsePage(posts);
    }

    /**
     * 인기순
     */
    public Page<PostResponse> getPopularPosts(Pageable pageable) {
        Page<Post> posts = postRepository.findAllOrderByPopularity(null, pageable);
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

        Long me = currentMemberIdOrNull();

        return posts.map(post -> {
            Member author = authors.get(post.getMemberId());

            long likeCount = postReactionRepository.countByPostIdAndType(post.getPostId(), ReactionType.HEART);
            long bookmarkCount = postBookmarkRepository.countByPostId(post.getPostId());

            boolean liked = false, bookmarked = false;
            if (me != null) {
                liked = postReactionRepository.existsByPostIdAndMemberIdAndType(post.getPostId(), me, ReactionType.HEART);
                bookmarked = postBookmarkRepository.existsByPostIdAndMemberId(post.getPostId(), me);
            }

            return PostResponse.from(post, author, likeCount, bookmarkCount, liked, bookmarked);
        });
    }

    private Long currentMemberIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) return null;
        String loginId = auth.getName();
        return memberRepository.findByLoginId(loginId).map(Member::getId).orElse(null);
    }
}