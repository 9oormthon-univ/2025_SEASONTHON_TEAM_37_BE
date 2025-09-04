package rebound.backend.category.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rebound.backend.category.dto.CategoryDto;
import rebound.backend.category.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDto.MainCategoryResponse>> getAllCategories() {
        List<CategoryDto.MainCategoryResponse> categories = categoryService.findAllGroupedByMainCategory();
        return ResponseEntity.ok(categories);
    }
}


