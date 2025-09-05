package rebound.backend.post.dto;

import rebound.backend.category.entity.MainCategory;
import rebound.backend.category.entity.SubCategory;
import rebound.backend.post.entity.Post;
import rebound.backend.post.entity.PostImage;

import java.util.List;

public record PostUpdateRequest(
        MainCategory mainCategory,
        SubCategory subCategory,
        String title,
        Boolean isAnonymous,
        List<PostImage> images,
        String situationContent,
        String failureContent,
        String learningContent,
        String nextStepContent,
        List<String> tags,
        Post.Status status
) {
}
