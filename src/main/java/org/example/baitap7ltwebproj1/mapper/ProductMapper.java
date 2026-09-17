package org.example.baitap7ltwebproj1.mapper;

import lombok.RequiredArgsConstructor;
import org.example.baitap7ltwebproj1.dto.CategoryDTO;
import org.example.baitap7ltwebproj1.dto.ProductDTO;
import org.example.baitap7ltwebproj1.entity.Category;
import org.example.baitap7ltwebproj1.entity.Product;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final ProductImageMapper imageMapper;

    // =====================================
    // ENTITY → DTO
    // =====================================
    public ProductDTO toDTO(Product entity) {
        if (entity == null) return null;

        // Map category
        Long categoryId = null;
        String categoryName = null;
        if (entity.getCategory() != null) {
            categoryId   = entity.getCategory().getId();
            categoryName = entity.getCategory().getName();
        }

        return ProductDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .price(entity.getPrice())
                .quantity(entity.getQuantity())
                .description(entity.getDescription())
                .categoryId(categoryId)
                .categoryName(categoryName)
                .images(
                    entity.getImages()
                          .stream()
                          .map(imageMapper::toDTO)
                          .collect(Collectors.toList())
                )
                .build();
    }

    // =====================================
    // DTO → ENTITY (tạo mới)
    // =====================================
    public Product toEntity(ProductDTO dto) {
        if (dto == null) return null;

        Product product = Product.builder()
                .id(dto.getId())
                .name(dto.getName())
                .price(dto.getPrice())
                .quantity(dto.getQuantity())
                .description(dto.getDescription())
                .build();

        // Gắn category nếu có categoryId
        if (dto.getCategoryId() != null) {
            Category cat = new Category();
            cat.setId(dto.getCategoryId());
            product.setCategory(cat);
        }

        return product;
    }

    // =====================================
    // UPDATE ENTITY từ DTO — giữ nguyên createdAt và images
    // =====================================
    public void updateEntity(ProductDTO dto, Product entity) {
        entity.setName(dto.getName());
        entity.setPrice(dto.getPrice());
        entity.setQuantity(dto.getQuantity());
        entity.setDescription(dto.getDescription());

        // Cập nhật category
        if (dto.getCategoryId() != null) {
            Category cat = new Category();
            cat.setId(dto.getCategoryId());
            entity.setCategory(cat);
        } else {
            entity.setCategory(null);
        }
    }
}
