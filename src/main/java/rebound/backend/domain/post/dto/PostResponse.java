package rebound.backend.domain.post.dto;

import lombok.Builder;
import lombok.Getter;
import rebound.backend.domain.post.entity.Post;
import rebound.backend.domain.tag.entity.Tag;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class PostResponse {
    private final Long postId;
    private final String title;
    private final LocalDateTime createdAt;

    private final List<String> tags;

    public static PostResponse from(Post post) {
        List<String> tagNames = post.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.toList());

        return PostResponse.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .createdAt(post.getCreatedAt())
                .tags(tagNames)
                .build();
    }
}

