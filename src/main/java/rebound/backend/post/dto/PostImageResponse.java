package rebound.backend.post.dto;

import lombok.Builder;
import lombok.Getter;
import rebound.backend.post.entity.PostImage;

@Getter
@Builder
public class PostImageResponse {
    private final Long id;
    private final String imageUrl;
    private final int imageOrder;

    public static PostImageResponse from(PostImage postImage) {
        return PostImageResponse.builder()
                .id(postImage.getId())
                .imageUrl(postImage.getImageUrl())
                .imageOrder(postImage.getImageOrder())
                .build();
    }
}
