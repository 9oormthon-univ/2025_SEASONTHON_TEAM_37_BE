package rebound.backend.post.repository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import rebound.backend.category.entity.MainCategory;
import rebound.backend.category.entity.SubCategory;
import rebound.backend.post.entity.Post;

import java.util.List;

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
     * 특정 memberId를 가진 게시글을 찾는 조건을 생성합니다.
     */
    public static Specification<Post> hasMemberId(Long memberId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("memberId"), memberId);
    }

    public static Specification<Post> inMainCategories(List<MainCategory> mainCategories) {
        return (root, query, criteriaBuilder) -> {
            if (mainCategories == null || mainCategories.isEmpty()) {
                return null;
            }
            return root.get("mainCategory").in(mainCategories);
        };
    }
}

