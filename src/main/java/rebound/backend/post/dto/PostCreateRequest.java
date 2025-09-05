package rebound.backend.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import rebound.backend.category.entity.MainCategory;
import rebound.backend.category.entity.SubCategory;

import java.util.List;

public record PostCreateRequest(
        @NotNull MainCategory mainCategory,
        @NotNull SubCategory subCategory,
        @NotBlank String title,
        Boolean isAnonymous,
        String imageUrl,

        String situationContent,
        String failureContent,
        String learningContent,
        String nextStepContent,

        List<String> tags
) {}
