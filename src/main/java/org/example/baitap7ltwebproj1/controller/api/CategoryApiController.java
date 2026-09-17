package org.example.baitap7ltwebproj1.controller.api;

import lombok.RequiredArgsConstructor;
import org.example.baitap7ltwebproj1.dto.CategoryDTO;
import org.example.baitap7ltwebproj1.model.ApiResponse;
import org.example.baitap7ltwebproj1.service.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * RESTful API cho Categories.
 * Base URL: /api/category
 */
@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryApiController {

    private final CategoryService categoryService;

    // =====================================================
    // GET ALL (không phân trang — dùng cho dropdown)
    // GET /api/category
    // =====================================================
    @GetMapping
    public ResponseEntity<?> getAllCategory() {
        return new ResponseEntity<>(
                new ApiResponse(true, "Thành công", categoryService.findAll()),
                HttpStatus.OK
        );
    }

    // =====================================================
    // SEARCH + PHÂN TRANG
    // GET /api/category/search?keyword=&page=0&size=5
    // =====================================================
    @GetMapping("/search")
    public ResponseEntity<?> searchCategories(
            @RequestParam(defaultValue = "")  String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Page<CategoryDTO> result = categoryService.findAll(keyword, page, size);

        int totalPages  = result.getTotalPages();
        int currentPage = result.getNumber();
        int startPage   = Math.max(0, currentPage - 2);
        int endPage     = Math.min(totalPages - 1, startPage + 4);
        if (endPage - startPage < 4) startPage = Math.max(0, endPage - 4);

        Map<String, Object> pageInfo = Map.of(
                "content",       result.getContent(),
                "currentPage",   currentPage,
                "totalPages",    totalPages,
                "totalElements", result.getTotalElements(),
                "startPage",     startPage,
                "endPage",       endPage,
                "first",         result.isFirst(),
                "last",          result.isLast()
        );

        return new ResponseEntity<>(
                new ApiResponse(true, "Thành công", pageInfo),
                HttpStatus.OK
        );
    }

    // =====================================================
    // GET BY ID
    // GET /api/category/{id}
    // =====================================================
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategory(@PathVariable Long id) {
        try {
            return new ResponseEntity<>(
                    new ApiResponse(true, "Thành công", categoryService.findById(id)),
                    HttpStatus.OK
            );
        } catch (RuntimeException e) {
            return new ResponseEntity<>(
                    new ApiResponse(false, "Không tìm thấy danh mục id=" + id, null),
                    HttpStatus.NOT_FOUND
            );
        }
    }

    // =====================================================
    // ADD CATEGORY
    // POST /api/category/addCategory
    // =====================================================
    @PostMapping(path = "/addCategory", consumes = "multipart/form-data")
    public ResponseEntity<?> addCategory(
            @RequestParam("categoryName")                            String categoryName,
            @RequestParam(value = "description", required = false)  String description,
            @RequestParam(value = "icon",        required = false)  MultipartFile icon) {
        try {
            CategoryDTO dto = new CategoryDTO();
            dto.setName(categoryName);
            dto.setDescription(description);
            dto.setIconFile(icon);
            return new ResponseEntity<>(
                    new ApiResponse(true, "Thêm danh mục thành công", categoryService.create(dto)),
                    HttpStatus.OK
            );
        } catch (RuntimeException e) {
            return new ResponseEntity<>(
                    new ApiResponse(false, e.getMessage(), null),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    // =====================================================
    // UPDATE CATEGORY
    // PUT /api/category/updateCategory
    // =====================================================
    @PutMapping(path = "/updateCategory", consumes = "multipart/form-data")
    public ResponseEntity<?> updateCategory(
            @RequestParam("categoryId")                              Long categoryId,
            @RequestParam("categoryName")                            String categoryName,
            @RequestParam(value = "description", required = false)  String description,
            @RequestParam(value = "icon",        required = false)  MultipartFile icon) {
        try {
            CategoryDTO dto = new CategoryDTO();
            dto.setName(categoryName);
            dto.setDescription(description);
            dto.setIconFile(icon);
            return new ResponseEntity<>(
                    new ApiResponse(true, "Cập nhật danh mục thành công", categoryService.update(categoryId, dto)),
                    HttpStatus.OK
            );
        } catch (RuntimeException e) {
            return new ResponseEntity<>(
                    new ApiResponse(false, e.getMessage(), null),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    // =====================================================
    // DELETE CATEGORY
    // DELETE /api/category/deleteCategory?categoryId=
    // =====================================================
    @DeleteMapping("/deleteCategory")
    public ResponseEntity<?> deleteCategory(@RequestParam("categoryId") Long categoryId) {
        try {
            CategoryDTO category = categoryService.findById(categoryId);
            categoryService.delete(categoryId);
            return new ResponseEntity<>(
                    new ApiResponse(true, "Xóa danh mục thành công", category),
                    HttpStatus.OK
            );
        } catch (RuntimeException e) {
            return new ResponseEntity<>(
                    new ApiResponse(false, e.getMessage(), null),
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}
