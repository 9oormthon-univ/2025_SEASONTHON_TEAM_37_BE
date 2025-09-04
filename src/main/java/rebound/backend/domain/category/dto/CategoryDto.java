package rebound.backend.domain.category.dto;

import lombok.Builder;
import lombok.Getter;
import rebound.backend.domain.category.entity.MainCategory;
import rebound.backend.domain.category.entity.SubCategory;

import java.util.List;
import java.util.stream.Collectors;

public class CategoryDto {

    @Getter
    @Builder
    public static class MainCategoryResponse {
        private final String mainCategoryCode; // e.g., "ADMISSION"
        private final String mainCategoryLabel; // e.g., "입시"
        private final List<SubCategoryResponse> subCategories;

        public static MainCategoryResponse from(MainCategory mainCategory) {
            // 해당 메인 카테고리에 속한 서브 카테고리 목록을 DTO로 변환
            List<SubCategoryResponse> subCategoryDtos = SubCategory.byMain(mainCategory)
                    .stream()
                    .map(SubCategoryResponse::from)
                    .collect(Collectors.toList());

            return MainCategoryResponse.builder()
                    .mainCategoryCode(mainCategory.name()) // Enum의 이름
                    .mainCategoryLabel(mainCategory.getLabel()) // Enum의 한글 라벨
                    .subCategories(subCategoryDtos)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class SubCategoryResponse {
        private final String subCategoryCode; // e.g., "ADMISSION_STRATEGY_ERROR"
        private final String subCategoryLabel; // e.g., "수시/정시 전략 오류"

        public static SubCategoryResponse from(SubCategory subCategory) {
            return SubCategoryResponse.builder()
                    .subCategoryCode(subCategory.name()) // Enum의 이름
                    .subCategoryLabel(subCategory.getLabel()) // Enum의 한글 라벨
                    .build();
        }
    }
}
