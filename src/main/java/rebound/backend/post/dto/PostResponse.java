package rebound.backend.post.dto;

import lombok.Builder;
import lombok.Getter;
import rebound.backend.post.entity.Post;
import rebound.backend.post.entity.PostContent;
import rebound.backend.post.entity.PostImage;
import rebound.backend.tag.entity.Tag;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class PostResponse {

    private final Long postId;
    private final String title;
    private final LocalDateTime createdAt;
    private final List<String> tags;
    private final List<String> imageUrls;
    private final String authorNickname;
    private final CategoryDetail category;
    private final String situationContent;
    private final String failureContent;
    private final String learningContent;
    private final String nextStepContent;

    // 인터랙션 표시용 필드 추가
    @Builder.Default private Long likeCount = 0L;
    @Builder.Default private Long bookmarkCount = 0L;
    @Builder.Default private Boolean liked = false;
    @Builder.Default private Boolean bookmarked = false;

    @Getter
    @Builder
    public static class CategoryDetail {
        private final String mainCategoryCode;
        private final String mainCategoryLabel;
        private final String subCategoryCode;
        private final String subCategoryLabel;

        public static CategoryDetail from(Post post) {
            return CategoryDetail.builder()
                    .mainCategoryCode(post.getMainCategory().name())
                    .mainCategoryLabel(post.getMainCategory().getLabel())
                    .subCategoryCode(post.getSubCategory().name())
                    .subCategoryLabel(post.getSubCategory().getLabel())
                    .build();
        }
    }

    /**
     * 게시글 생성/수정 등 닉네임 정보가 없을 때 호출하는 메서드
     */
    public static PostResponse from(Post post) {
        return from(post, null);
    }

    /**
     * 게시글 상세 조회 등 모든 정보가 필요할 때 호출하는 완전한 메서드
     */
    public static PostResponse from(Post post, String authorNickname) {
        List<String> tagNames = post.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.toList());

        List<String> imageUrls = (post.getPostImages() != null)
                ? post.getPostImages().stream().map(PostImage::getImageUrl).collect(Collectors.toList())
                : Collections.emptyList();

        // PostContent가 null일 경우를 대비한 방어 코드
        PostContent content = post.getPostContent();

        return PostResponse.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .createdAt(post.getCreatedAt())
                .tags(tagNames)
                .imageUrls(imageUrls)
                .authorNickname(authorNickname)
                .category(CategoryDetail.from(post))
                .situationContent(content != null ? content.getSituationContent() : null)
                .failureContent(content != null ? content.getFailureContent() : null)
                .learningContent(content != null ? content.getLearningContent() : null)
                .nextStepContent(content != null ? content.getNextStepContent() : null)
                .build();
    }

    // 서비스에서 주입할 수 있도록 세터 추가
    public void setLikeCount(long likeCount) { this.likeCount = likeCount; }
    public void setBookmarkCount(long bookmarkCount) { this.bookmarkCount = bookmarkCount; }
    public void setLiked(boolean liked) { this.liked = liked; }
    public void setBookmarked(boolean bookmarked) { this.bookmarked = bookmarked; }
}