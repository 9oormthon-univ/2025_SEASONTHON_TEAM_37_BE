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

    /**
     * 작성자 정보를 담는 내부 DTO
     */
    @Getter
    @Builder
    public static class AuthorDetail {
        private final Long memberId;
        private final String nickname;
        private final String profileImage;
    }

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
     * Post 엔티티와 Member 엔티티를 받아 완전한 PostResponse DTO를 생성합니다.
     */
    public static PostResponse from(Post post, Member author) {
        AuthorDetail authorDetail;
        if (author != null) {
            if (post.getIsAnonymous()) {
                // 익명일 경우
                authorDetail = AuthorDetail.builder()
                        .memberId(author.getId())
                        .nickname(NicknameMasker.mask(author.getNickname()))
                        .profileImage(null)
                        .build();
            } else {
                // 익명이 아닐 경우
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
            // 작성자 정보가 없는 경우
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
                .author(authorDetail) // author 객체 설정
                .tags(tagNames)
                .imageUrls(imageUrls)
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