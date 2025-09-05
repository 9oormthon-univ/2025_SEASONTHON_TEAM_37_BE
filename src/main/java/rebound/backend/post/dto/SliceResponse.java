package rebound.backend.post.dto;

import java.util.List;

public record SliceResponse<T>(
        List<T> items,
        boolean hasNext,
        int page,
        int size
) {
    public static <T> SliceResponse<T> of(org.springframework.data.domain.Slice<T> s) {
        return new SliceResponse<>(s.getContent(), s.hasNext(), s.getNumber(), s.getSize());
    }
}
