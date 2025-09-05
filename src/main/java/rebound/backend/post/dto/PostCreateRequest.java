package rebound.backend.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import rebound.backend.category.entity.MainCategory;
import rebound.backend.category.entity.SubCategory;
import rebound.backend.post.entity.PostImage;

import java.util.List;

public record PostCreateRequest(
        @NotNull MainCategory mainCategory,
        @NotNull SubCategory subCategory,
        @NotBlank String title,
        Boolean isAnonymous,
        List<PostImage> images,

        String situationContent,
        String failureContent,
        String learningContent,
        String nextStepContent,

        List<String> tags
) {}
