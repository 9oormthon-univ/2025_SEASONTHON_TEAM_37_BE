package rebound.backend.post.repository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import rebound.backend.category.entity.MainCategory;
import rebound.backend.category.entity.SubCategory;
import rebound.backend.post.entity.Post;
import rebound.backend.post.entity.PostReaction;
import rebound.backend.post.entity.PostBookmark;
import rebound.backend.post.entity.Comment;

public class PostSpecification {

    public static Specification<Post> searchByKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            // 키워드가 비어있으면 아무 조건도 적용하지 않음 (모든 게시글 반환)
            if (!StringUtils.hasText(keyword)) {
                return null;
            }

            // 검색 조건들을 담을 리스트 생성
            Predicate titlePredicate = criteriaBuilder.like(root.get("title"), "%" + keyword + "%");

            // PostContent의 각 필드에서 키워드 검색
            Predicate situationContentPredicate = criteriaBuilder.like(root.get("postContent").get("situationContent"), "%" + keyword + "%");
            Predicate failureContentPredicate = criteriaBuilder.like(root.get("postContent").get("failureContent"), "%" + keyword + "%");
            Predicate learningContentPredicate = criteriaBuilder.like(root.get("postContent").get("learningContent"), "%" + keyword + "%");
            Predicate nextStepContentPredicate = criteriaBuilder.like(root.get("postContent").get("nextStepContent"), "%" + keyword + "%");

            return criteriaBuilder.or(
                    titlePredicate,
                    situationContentPredicate,
                    failureContentPredicate,
                    learningContentPredicate,
                    nextStepContentPredicate
            );
        };
    }
    /**
     * 대분류 필터링 조건
     */
    public static Specification<Post> hasMainCategory(MainCategory mainCategory) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("mainCategory"), mainCategory);
    }

    /**
     * 소분류 필터링 조건
     */
    public static Specification<Post> hasSubCategory(SubCategory subCategory) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("subCategory"), subCategory);
    }

    /**
     * 내가 작성한 게시글 필터링
     */
    public static Specification<Post> hasAuthor(Long memberId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("memberId"), memberId);
    }

    /**
     * 내가 좋아요한 게시글 필터링
     */
    public static Specification<Post> likedByMember(Long memberId) {
        return (root, query, criteriaBuilder) -> {
            var subquery = query.subquery(Long.class);
            var reactionRoot = subquery.from(PostReaction.class);
            subquery.select(reactionRoot.get("postId")); // postId 필드 직접 사용
            subquery.where(criteriaBuilder.and(
                    criteriaBuilder.equal(reactionRoot.get("memberId"), memberId),
                    criteriaBuilder.equal(reactionRoot.get("type"), "HEART") // HEART 타입 사용
            ));
            return criteriaBuilder.in(root.get("postId")).value(subquery);
        };
    }

    /**
     * 내가 댓글단 게시글 필터링
     */
    public static Specification<Post> commentedByMember(Long memberId) {
        return (root, query, criteriaBuilder) -> {
            var subquery = query.subquery(Long.class);
            var commentRoot = subquery.from(Comment.class);
            subquery.select(commentRoot.get("postId")); // postId 필드 직접 사용
            subquery.where(criteriaBuilder.equal(commentRoot.get("memberId"), memberId));
            return criteriaBuilder.in(root.get("postId")).value(subquery);
        };
    }

    /**
     * 내가 북마크한 게시글 필터링
     */
    public static Specification<Post> bookmarkedByMember(Long memberId) {
        return (root, query, criteriaBuilder) -> {
            var subquery = query.subquery(Long.class);
            var bookmarkRoot = subquery.from(PostBookmark.class);
            subquery.select(bookmarkRoot.get("postId")); // postId 필드 직접 사용
            subquery.where(criteriaBuilder.equal(bookmarkRoot.get("memberId"), memberId));
            return criteriaBuilder.in(root.get("postId")).value(subquery);
        };
    }

    /**
     * 좋아요 수 기준으로 정렬하기 위한 Specification
     * (실제로는 Repository에서 직접 처리하는 것이 더 효율적)
     */
    public static Specification<Post> orderByLikes() {
        return (root, query, criteriaBuilder) -> {
            // 이 메서드는 실제로는 사용되지 않음
            // 정렬은 Repository 레벨에서 처리
            return null;
        };
    }
}

