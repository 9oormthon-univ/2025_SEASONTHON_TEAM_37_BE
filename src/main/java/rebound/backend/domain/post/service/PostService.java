package rebound.backend.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rebound.backend.domain.post.dto.PostCreateRequest;
import rebound.backend.domain.post.dto.PostResponse;
import rebound.backend.domain.post.entity.Post;
import rebound.backend.domain.post.entity.PostContent;
import rebound.backend.domain.post.repository.PostRepository;
import rebound.backend.domain.tag.entity.Tag;
import rebound.backend.domain.tag.repository.TagRepository;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;

    /**
     * 기능: 새 글을 '발행' 또는 '임시 저장' 상태로 생성
     */
    @Transactional
    public PostResponse createPost(PostCreateRequest request) {
        // 1. PostContent 엔티티 생성
        PostContent postContent = PostContent.builder()
                .situationContent(request.situationContent())
                .failureContent(request.failureContent())
                .learningContent(request.learningContent())
                .nextStepContent(request.nextStepContent())
                .build();


        Post.Status finalstatus = (request.publish() != null && request.publish())
                ? Post.Status.PUBLIC
                : Post.Status.DRAFT;

        // 2. Post 엔티티 생성
        Post post = Post.builder()
                .memberId(request.memberId())
                .mainCategory(request.mainCategory())
                .subCategory(request.subCategory())
                .title(request.title())
                .isAnonymous(request.isAnonymous() != null ? request.isAnonymous() : Boolean.FALSE) // null 체크
                .imageUrl(request.imageUrl())
                .status(finalstatus)
                .build();

        // 3. 연관관계 설정 (양방향)
        post.setPostContent(postContent);
        postContent.setPost(post);

        if (request.tags() != null && !request.tags().isEmpty()) {
            request.tags().forEach(tagName -> {
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()));
                post.addTag(tag); // Post 엔티티에 추가한 연관관계 편의 메서드 사용
            });
        }

        Post savedPost = postRepository.save(post);

        return PostResponse.from(savedPost);
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
}
