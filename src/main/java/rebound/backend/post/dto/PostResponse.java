package rebound.backend.post.dto;

import lombok.Builder;
import lombok.Getter;
import rebound.backend.member.domain.Member;
import rebound.backend.post.entity.Post;
import rebound.backend.post.entity.PostContent;
import rebound.backend.post.entity.PostImage;
import rebound.backend.tag.entity.Tag;
import rebound.backend.utils.NicknameMasker;

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
    private final CategoryDetail category;
    private final String situationContent;
    private final String failureContent;
    private final String learningContent;
    private final String nextStepContent;
    private final AuthorDetail author;

    private final Long likeCount;
    private final Long bookmarkCount;
    private final Boolean liked;
    private final Boolean bookmarked;

    @Getter
    @Builder
    public static class AuthorDetail {
        private final Long memberId;
        private final String nickname;
        private final String profileImage;
        private final boolean hasRankBadge;
    }

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

    public static PostResponse from(Post post, Member author, long likeCount, long bookmarkCount, boolean liked, boolean bookmarked, boolean hasRankBadge) {
        AuthorDetail authorDetail;
        if (author != null) {
            if (post.getIsAnonymous()) {
                authorDetail = AuthorDetail.builder()
                        .memberId(author.getId())
                        .nickname(NicknameMasker.mask(author.getNickname()))
                        .profileImage(null)
                        .build();
            } else {
                String profileImageUrl = (author.getMemberImage() != null)
                        ? author.getMemberImage().getImageUrl()
                        : null;
                authorDetail = AuthorDetail.builder()
                        .memberId(author.getId())
                        .nickname(author.getNickname())
                        .profileImage(profileImageUrl)
                        .build();
            }
        } else {
            authorDetail = AuthorDetail.builder()
                    .memberId(null)
                    .nickname("알 수 없는 사용자")
                    .profileImage(null)
                    .build();
        }

        List<String> tagNames = (post.getTags() != null)
                ? post.getTags().stream().map(Tag::getName).collect(Collectors.toList())
                : Collections.emptyList();

        List<String> imageUrls = (post.getPostImages() != null)
                ? post.getPostImages().stream().map(PostImage::getImageUrl).collect(Collectors.toList())
                : Collections.emptyList();

        PostContent content = post.getPostContent();

        return PostResponse.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .createdAt(post.getCreatedAt())
                .author(authorDetail)
                .tags(tagNames)
                .imageUrls(imageUrls)
                .category(CategoryDetail.from(post))
                .situationContent(content != null ? content.getSituationContent() : null)
                .failureContent(content != null ? content.getFailureContent() : null)
                .learningContent(content != null ? content.getLearningContent() : null)
                .nextStepContent(content != null ? content.getNextStepContent() : null)
                .likeCount(likeCount)
                .bookmarkCount(bookmarkCount)
                .liked(liked)
                .bookmarked(bookmarked)
                .build();
    }
}