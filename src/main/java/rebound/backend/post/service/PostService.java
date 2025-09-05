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
import rebound.backend.utils.InteractionAuth;

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
     * 통합 게시글 목록 조회 (다양한 필터링 옵션 지원)
     */
    public Page<PostResponse> getPosts(
            MainCategory mainCategory, 
            SubCategory subCategory, 
            String keyword, 
            String sort, 
            String filter, 
            Pageable pageable) {
        
        Specification<Post> spec = (root, query, criteriaBuilder) -> null;
        
        // 카테고리 필터링
        if (subCategory != null) {
            spec = spec.and(PostSpecification.hasSubCategory(subCategory));
        }
        if (mainCategory != null) {
            spec = spec.and(PostSpecification.hasMainCategory(mainCategory));
        }
        
        // 키워드 검색
        if (keyword != null && !keyword.trim().isEmpty()) {
            spec = spec.and(PostSpecification.searchByKeyword(keyword));
        }
        
        // 개인 필터링 (인증된 사용자만)
        if (filter != null && !filter.trim().isEmpty()) {
            try {
                Long currentMemberId = InteractionAuth.currentMemberId();
                
                switch (filter.toLowerCase()) {
                    case "my_posts":
                        spec = spec.and(PostSpecification.hasAuthor(currentMemberId));
                        break;
                    case "my_likes":
                        spec = spec.and(PostSpecification.likedByMember(currentMemberId));
                        break;
                    case "my_comments":
                        spec = spec.and(PostSpecification.commentedByMember(currentMemberId));
                        break;
                    case "my_bookmarks":
                        spec = spec.and(PostSpecification.bookmarkedByMember(currentMemberId));
                        break;
                    default:
                        // 알 수 없는 필터는 무시
                        break;
                }
            } catch (Exception e) {
                // 인증되지 않은 사용자는 개인 필터를 사용할 수 없음
                throw new IllegalArgumentException("개인 필터를 사용하려면 로그인이 필요합니다.");
            }
        }
        
        Page<Post> posts = postRepository.findAll(spec, pageable);
        return mapToPostResponsePage(posts);
    }

    /**
     * 카테고리별 게시글 목록 조회 (기존 메서드 - 하위 호환성 유지)
     */
    public Page<PostResponse> getPosts(MainCategory mainCategory, SubCategory subCategory, Pageable pageable) {
        return getPosts(mainCategory, subCategory, null, null, null, pageable);
    }



    /**
     * 게시글 상세 조회
     */
    public PostResponse getPostDetails(Long postId) {
        Post post = findPostById(postId);
        Member author = memberRepository.findById(post.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("작성자 정보를 찾을 수 없습니다."));

        String finalNickname = post.getIsAnonymous()
                ? NicknameMasker.mask(author.getNickname())
                : author.getNickname();

        return PostResponse.from(post, finalNickname);
    }

    /**
     * 게시글 생성 (여러 이미지 포함)
     */
    @Transactional
    public PostResponse createPostWithImages(PostCreateRequest request, List<MultipartFile> files) throws IOException {
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        Member currentMember = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("현재 로그인된 사용자 정보를 찾을 수 없습니다."));

        PostContent postContent = PostContent.builder()
                .situationContent(request.situationContent())
                .failureContent(request.failureContent())
                .learningContent(request.learningContent())
                .nextStepContent(request.nextStepContent())
                .build();

        Post post = Post.builder()
                .memberId(currentMember.getId())
                .mainCategory(request.mainCategory()).subCategory(request.subCategory())
                .title(request.title()).isAnonymous(request.isAnonymous() != null ? request.isAnonymous() : Boolean.FALSE)
                .build();

        post.setPostContent(postContent);
        postContent.setPost(post);

        // 여러 이미지 업로드 및 PostImage 엔티티 생성/연결
        if (files != null && !files.isEmpty()) {
            List<String> imageUrls = s3Service.uploadFiles(files);
            for (int i = 0; i < imageUrls.size(); i++) {
                PostImage postImage = PostImage.builder()
                        .imageUrl(imageUrls.get(i))
                        .imageOrder(i)
                        .build();
                post.addImage(postImage);
            }
        }

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


    /**
     * 게시글 수정
     */
    @Transactional
    public PostResponse updatePost(Long postId, PostUpdateRequest request, List<MultipartFile> files) throws IOException {
        Post post = findPostById(postId);
        authorizePostAuthor(post);

        // 이미지 업데이트
        post.getPostImages().clear();
        if (files != null && !files.isEmpty()) {
            List<String> imageUrls = s3Service.uploadFiles(files);
            for (int i = 0; i < imageUrls.size(); i++) {
                PostImage postImage = PostImage.builder()
                        .imageUrl(imageUrls.get(i))
                        .imageOrder(i).build();
                post.addImage(postImage);
            }
        }

        // 내용 업데이트
        post.setMainCategory(request.mainCategory());
        post.setSubCategory(request.subCategory());
        post.setTitle(request.title());
        post.setIsAnonymous(request.isAnonymous());
        PostContent postContent = post.getPostContent();
        postContent.setSituationContent(request.situationContent());
        postContent.setFailureContent(request.failureContent());
        postContent.setLearningContent(request.learningContent());
        postContent.setNextStepContent(request.nextStepContent());

        // 태그 업데이트
        if (request.tags() != null) {
            post.getTags().clear();
            request.tags().forEach(tagName -> {
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()));
                post.addTag(tag);
            });
        }

        // 상태 변경 로직 통합
        if (request.status() != null) {
            post.setStatus(request.status());
        }

        Member author = memberRepository.findById(post.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("작성자 정보를 찾을 수 없습니다."));
        String finalNickname = post.getIsAnonymous()
                ? NicknameMasker.mask(author.getNickname())
                : author.getNickname();
        return PostResponse.from(post, finalNickname);
    }



    @Transactional
    public void deletePost(Long postId) {
        Post post = findPostById(postId);
        authorizePostAuthor(post);
        postRepository.delete(post);
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

    private Page<PostResponse> mapToPostResponsePage(Page<Post> posts) {
        List<Long> authorIds = posts.getContent().stream()
                .map(Post::getMemberId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Member> authors = memberRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(Member::getId, member -> member));

        return posts.map(post -> {
            Member author = authors.get(post.getMemberId());
            String nickname = (author != null) ? author.getNickname() : "알 수 없는 사용자";
            String finalNickname = post.getIsAnonymous() ? NicknameMasker.mask(nickname) : nickname;
            return PostResponse.from(post, finalNickname);
        });
    }
}
