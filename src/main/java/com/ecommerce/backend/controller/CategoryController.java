package com.ecommerce.backend.controller;

import com.ecommerce.backend.entity.Category;
import com.ecommerce.backend.repository.CategoryRepository;
import com.ecommerce.backend.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // ✅ FIX: Use @RequestBody to receive JSON from React
    @PostMapping
    public Category createCategory(@RequestBody Category category) {
        return categoryRepository.save(category);
    }

    // ✅ FIX: Use @RequestBody and update the existing entity
    @PutMapping("/{id}")
    public Category updateCategory(@PathVariable Long id, @RequestBody Category categoryDetails) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        category.setName(categoryDetails.getName());
        category.setImage(categoryDetails.getImage()); // This is now the Cloudinary URL string

        return categoryRepository.save(category);
    }

    @DeleteMapping("/{id}")
    public void deleteCategory(@PathVariable Long id) {
        categoryRepository.deleteById(id);
    }
}