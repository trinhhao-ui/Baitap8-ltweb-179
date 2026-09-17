package org.example.baitap7ltwebproj1.controller.api;

import lombok.RequiredArgsConstructor;
import org.example.baitap7ltwebproj1.dto.ProductDTO;
import org.example.baitap7ltwebproj1.model.ApiResponse;
import org.example.baitap7ltwebproj1.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * RESTful API cho Products.
 * Base URL: /api/product
 */
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductApiController {

    private final ProductService productService;

    // =====================================================
    // GET ALL (không phân trang — dùng nội bộ nếu cần)
    // GET /api/product
    // =====================================================
    @GetMapping
    public ResponseEntity<?> getAllProduct() {
        List<ProductDTO> products = productService.findAll();
        return new ResponseEntity<>(
                new ApiResponse(true, "Thành công", products),
                HttpStatus.OK
        );
    }

    // =====================================================
    // SEARCH + PHÂN TRANG
    // GET /api/product/search?keyword=&page=0&size=5
    // =====================================================
    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(
            @RequestParam(defaultValue = "")  String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Page<ProductDTO> result = productService.findAll(keyword, page, size);

        // Tính cửa sổ phân trang (5 nút)
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
    // GET /api/product/{id}
    // =====================================================
    @GetMapping("/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Long id) {
        try {
            return new ResponseEntity<>(
                    new ApiResponse(true, "Thành công", productService.findById(id)),
                    HttpStatus.OK
            );
        } catch (RuntimeException e) {
            return new ResponseEntity<>(
                    new ApiResponse(false, "Không tìm thấy sản phẩm id=" + id, null),
                    HttpStatus.NOT_FOUND
            );
        }
    }

    // =====================================================
    // ADD PRODUCT
    // POST /api/product/addProduct
    // =====================================================
    @PostMapping(path = "/addProduct", consumes = "multipart/form-data")
    public ResponseEntity<?> addProduct(
            @RequestParam("name")                                     String name,
            @RequestParam("price")                                    java.math.BigDecimal price,
            @RequestParam("quantity")                                 Integer quantity,
            @RequestParam(value = "description",  required = false)  String description,
            @RequestParam(value = "categoryId",   required = false)  Long categoryId,
            @RequestParam(value = "imageFiles",   required = false)  List<MultipartFile> imageFiles) {
        try {
            ProductDTO dto = new ProductDTO();
            dto.setName(name);
            dto.setPrice(price);
            dto.setQuantity(quantity);
            dto.setDescription(description);
            dto.setCategoryId(categoryId);
            dto.setImageFiles(imageFiles);
            return new ResponseEntity<>(
                    new ApiResponse(true, "Thêm sản phẩm thành công", productService.create(dto)),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse(false, "Không thể thêm sản phẩm: " + e.getMessage(), null),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    // =====================================================
    // UPDATE PRODUCT
    // PUT /api/product/updateProduct
    // =====================================================
    @PutMapping(path = "/updateProduct", consumes = "multipart/form-data")
    public ResponseEntity<?> updateProduct(
            @RequestParam("id")                                       Long id,
            @RequestParam("name")                                     String name,
            @RequestParam("price")                                    java.math.BigDecimal price,
            @RequestParam("quantity")                                 Integer quantity,
            @RequestParam(value = "description",  required = false)  String description,
            @RequestParam(value = "categoryId",   required = false)  Long categoryId,
            @RequestParam(value = "imageFiles",   required = false)  List<MultipartFile> imageFiles) {
        try {
            ProductDTO dto = new ProductDTO();
            dto.setName(name);
            dto.setPrice(price);
            dto.setQuantity(quantity);
            dto.setDescription(description);
            dto.setCategoryId(categoryId);
            dto.setImageFiles(imageFiles);
            return new ResponseEntity<>(
                    new ApiResponse(true, "Cập nhật thành công", productService.update(id, dto)),
                    HttpStatus.OK
            );
        } catch (RuntimeException e) {
            return new ResponseEntity<>(
                    new ApiResponse(false, "Không thể cập nhật: " + e.getMessage(), null),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    // =====================================================
    // DELETE PRODUCT
    // DELETE /api/product/deleteProduct?id=
    // =====================================================
    @DeleteMapping("/deleteProduct")
    public ResponseEntity<?> deleteProduct(@RequestParam("id") Long id) {
        try {
            ProductDTO product = productService.findById(id);
            productService.delete(id);
            return new ResponseEntity<>(
                    new ApiResponse(true, "Xóa sản phẩm thành công", product),
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
    // DELETE ONE IMAGE
    // DELETE /api/product/deleteImage?imageId=
    // =====================================================
    @DeleteMapping("/deleteImage")
    public ResponseEntity<?> deleteImage(@RequestParam("imageId") Long imageId) {
        try {
            Long productId = productService.deleteImage(imageId);
            return new ResponseEntity<>(
                    new ApiResponse(true, "Xóa hình ảnh thành công", productId),
                    HttpStatus.OK
            );
        } catch (RuntimeException e) {
            return new ResponseEntity<>(
                    new ApiResponse(false, "Không thể xóa hình ảnh: " + e.getMessage(), null),
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}
