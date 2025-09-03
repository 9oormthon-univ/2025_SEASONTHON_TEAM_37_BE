package rebound.backend.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rebound.backend.domain.post.dto.PostCreateRequest;
import rebound.backend.domain.post.dto.PostResponse;
import rebound.backend.domain.post.entity.Post;
import rebound.backend.domain.post.entity.PostContent;
import rebound.backend.domain.post.repository.PostRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;

    @Transactional
    public PostResponse createPost(PostCreateRequest request) {
        // 1. PostContent 엔티티 생성
        PostContent postContent = PostContent.builder()
                .situationContent(request.situationContent())
                .failureContent(request.failureContent())
                .learningContent(request.learningContent())
                .nextStepContent(request.nextStepContent())
                .build();

        // 2. Post 엔티티 생성
        Post post = Post.builder()
                .memberId(request.memberId())
                .mainCategory(request.mainCategory())
                .subCategory(request.subCategory())
                .title(request.title())
                .isAnonymous(request.isAnonymous() != null ? request.isAnonymous() : Boolean.FALSE) // null 체크
                .imageUrl(request.imageUrl())
                .status(Post.Status.PUBLIC)
                .build();

        // 3. 연관관계 설정 (양방향)
        // PostContent에 Post를 설정하고, Post에 PostContent를 설정합니다.
        post.setPostContent(postContent);
        postContent.setPost(post);

        // 4. Post 저장 (PostContent는 Post에 의해 자동으로 저장됨)
        Post savedPost = postRepository.save(post);

        // 5. 응답 DTO로 변환하여 반환
        return PostResponse.from(savedPost);
    }
}
