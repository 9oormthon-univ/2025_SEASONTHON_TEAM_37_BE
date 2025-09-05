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
import rebound.backend.post.entity.PostImage;
import rebound.backend.post.repository.PostRepository;
import rebound.backend.s3.service.S3Service;
import rebound.backend.tag.entity.Tag;
import rebound.backend.tag.repository.TagRepository;
import rebound.backend.utils.NicknameMasker;

import java.io.IOException;
import java.util.List;

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
}
