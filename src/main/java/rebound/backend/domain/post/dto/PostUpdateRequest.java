package rebound.backend.domain.post.dto;

import rebound.backend.domain.category.entity.MainCategory;
import rebound.backend.domain.category.entity.SubCategory;

import java.util.List;

public record PostUpdateRequest(
        MainCategory mainCategory,
        SubCategory subCategory,
        String title,
        Boolean isAnonymous,
        String situationContent,
        String failureContent,
        String learningContent,
        String nextStepContent,
        List<String> tags
) {
}
