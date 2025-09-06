package rebound.backend.post.dto;

public record MyInteractionStateDto(
        boolean liked,
        boolean bookmarked,
        long likeCount,
        long bookmarkCount
) {}
