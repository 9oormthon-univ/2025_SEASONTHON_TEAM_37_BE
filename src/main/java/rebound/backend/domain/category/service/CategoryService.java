package rebound.backend.domain.category.service;

import org.springframework.stereotype.Service;
import rebound.backend.domain.category.dto.CategoryDto;
import rebound.backend.domain.category.entity.MainCategory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    /**
     * 모든 카테고리 정보를 대분류 기준으로 그룹화하여 반환합니다.
     * @return 대분류 목록 (각 대분류는 소분류 목록을 포함)
     */
    public List<CategoryDto.MainCategoryResponse> findAllGroupedByMainCategory() {
        // 모든 MainCategory Enum 값을 순회하면서
        return Arrays.stream(MainCategory.values())
                // 각각을 MainCategoryResponse DTO로 변환합니다.
                .map(CategoryDto.MainCategoryResponse::from)
                .collect(Collectors.toList());
    }
}
