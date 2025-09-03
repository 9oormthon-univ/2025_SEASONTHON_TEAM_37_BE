package rebound.backend.domain.post.dto;

import lombok.Builder;
import lombok.Getter;
import rebound.backend.domain.post.entity.Post;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostResponse {
    private final Long postId;
    private final String title;
    private final LocalDateTime createdAt;

    public static PostResponse from(Post post) {
        return PostResponse.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .createdAt(post.getCreatedAt())
                .build();
    }
}

