package rebound.backend.post.dto;

import lombok.Builder;
import lombok.Getter;
import rebound.backend.post.entity.Post;
import rebound.backend.tag.entity.Tag;

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
    private final String imageUrl;

    public static PostResponse from(Post post) {
        List<String> tagNames = post.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.toList());

        return PostResponse.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .createdAt(post.getCreatedAt())
                .tags(tagNames)
                .imageUrl(post.getImageUrl())
                .build();
    }
}
