package rebound.backend.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import rebound.backend.domain.category.entity.MainCategory;
import rebound.backend.domain.category.entity.SubCategory;

public record PostCreateRequest(
        @NotNull Long memberId,
        @NotNull MainCategory mainCategory,
        @NotNull SubCategory subCategory,
        @NotBlank String title,
        Boolean isAnonymous,
        String imageUrl,

        String situationContent,
        String failureContent,
        String learningContent,
        String nextStepContent
) {}
