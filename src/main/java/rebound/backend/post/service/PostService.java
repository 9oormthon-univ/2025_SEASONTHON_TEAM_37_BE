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

import java.util.*;
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
     * 추천 실패담 목록 조회 (관심사 기반)
     */
    public Page<PostResponse> getRecommendedPosts(Pageable pageable) {
        Long currentMemberId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        Member currentMember = memberRepository.findById(currentMemberId)
                .orElseThrow(() -> new IllegalArgumentException("현재 로그인된 사용자 정보를 찾을 수 없습니다."));

        List<MainCategory> interests = currentMember.getInterests().stream()
                .map(interest -> interest.getMainCategory())
                .collect(Collectors.toList());

        if (interests.isEmpty()) {
            return Page.empty(pageable);
        }

        Specification<Post> spec = PostSpecification.isPublic();
        spec = spec.and(PostSpecification.inMainCategories(interests));

        Page<Post> posts = postRepository.findAll(spec, pageable);
        return mapToPostResponsePage(posts);
    }

    /**
     * 카테고리별 게시글 목록 조회
     */
    public Page<PostResponse> getPosts(MainCategory mainCategory, SubCategory subCategory, Pageable pageable) {
        if (subCategory == null) {
            return Page.empty(pageable);
        }

        Specification<Post> spec = PostSpecification.isPublic();
        spec = spec.and(PostSpecification.hasSubCategory(subCategory));

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
        Specification<Post> spec = PostSpecification.isPublic();
        Page<Post> posts = postRepository.findAll(spec, pageable);
        return mapToPostResponsePage(posts);
    }

    /**
     * 게시글 상세 조회
     */
    @Transactional(readOnly = true)
    public PostResponse getPostDetails(Long postId) {
        Post post = findPostById(postId);
        Member author = memberRepository.findById(post.getMemberId())
                .orElse(null);

        long likeCount = postReactionRepository.countByPostIdAndType(postId, ReactionType.HEART);
        long bookmarkCount = postBookmarkRepository.countByPostId(postId);

        // 작성자의 총 좋아요수 조회
        long totalLikes = postReactionRepository.countByMemberIdAndType(author.getId(), ReactionType.HEART);
        boolean hasRankBadge = (totalLikes >= 10); // 10개 이상일 경우 true

        Long me = currentMemberIdOrNull();
        boolean liked = false, bookmarked = false;
        if (me != null) {
            liked = postReactionRepository.existsByPostIdAndMemberIdAndType(postId, me, ReactionType.HEART);
            bookmarked = postBookmarkRepository.existsByPostIdAndMemberId(postId, me);
        }

        return PostResponse.from(post, author, likeCount, bookmarkCount, liked, bookmarked, hasRankBadge);
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

        return PostResponse.from(savedPost, currentMember, 0L, 0L, false, false, false);
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
        long totalLikes =
                postReactionRepository.countByMemberIdAndType(author.getId(), ReactionType.HEART);
        boolean hasRankBadge = (totalLikes >= 10);
        return PostResponse.from(post, author, likeCount, bookmarkCount, liked, bookmarked, hasRankBadge);
    }

    /**
     * 게시글 검색 (키워드 기반, 페이징 포함)
     */
    public Page<PostResponse> searchPostsByKeyword(String keyword, Pageable pageable) {
        Specification<Post> spec = PostSpecification.isPublic();

        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and(PostSpecification.searchByKeyword(keyword));
        }

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

    /**
     * 게시글 목록을 DTO 페이지로 변환하고 좋아요/북마크 여부 확인
     * N+1 쿼리 문제 해결
     */
    private Page<PostResponse> mapToPostResponsePage(Page<Post> posts) {
        if (posts.isEmpty()) return Page.empty();

        List<Long> postIds = posts.getContent().stream()
                .map(Post::getPostId)
                .collect(Collectors.toList());

        // 1. 좋아요 및 북마크 수 미리 조회 (쿼리 2번)
        Map<Long, Long> likeCounts = postReactionRepository.countByPostIds(postIds, ReactionType.HEART);
        Map<Long, Long> bookmarkCounts = postBookmarkRepository.countByPostIds(postIds);

        // 2. 작성자 및 랭크 배지 정보 미리 조회
        List<Long> authorIds = posts.getContent().stream()
                .map(Post::getMemberId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Member> authors = memberRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(Member::getId, member -> member));

        List<Object[]> totalLikesList = postReactionRepository.countTotalLikesByMemberIds(authorIds, ReactionType.HEART);
        Map<Long, Long> totalLikesMap = totalLikesList.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0], // memberId
                        row -> (Long) row[1]  // totalLikes
                ));

        // 3. 현재 사용자의 좋아요 및 북마크 여부 미리 조회 (N+1 쿼리 해결)
        Long me = currentMemberIdOrNull();

        // ⭐️ 삼항 연산자를 사용하여 변수를 선언과 동시에 초기화
        Set<Long> likedPostIds = (me != null) ?
                postReactionRepository.findLikedPostIdsByMemberIdAndPostIds(me, postIds, ReactionType.HEART) :
                Collections.emptySet();

        Set<Long> bookmarkedPostIds = (me != null) ?
                postBookmarkRepository.findBookmarkedPostIdsByMemberIdAndPostIds(me, postIds) :
                Collections.emptySet();

        // 4. DTO로 변환
        return posts.map(post -> {
            Member author = authors.get(post.getMemberId());
            long totalLikes = totalLikesMap.getOrDefault(author.getId(), 0L);
            boolean hasRankBadge = (totalLikes >= 10);

            boolean liked = likedPostIds.contains(post.getPostId());
            boolean bookmarked = bookmarkedPostIds.contains(post.getPostId());
            long likeCount = likeCounts.getOrDefault(post.getPostId(), 0L);
            long bookmarkCount = bookmarkCounts.getOrDefault(post.getPostId(), 0L);

            return PostResponse.from(post, author, likeCount, bookmarkCount, liked, bookmarked, hasRankBadge);
        });
    }

    private Long currentMemberIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) return null;
        String loginId = auth.getName();
        return memberRepository.findByLoginId(loginId).map(Member::getId).orElse(null);
    }
}