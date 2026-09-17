package org.example.baitap7ltwebproj1.mapper;

import org.springframework.stereotype.Component;
import org.example.baitap7ltwebproj1.dto.ProductImageDTO;
import org.example.baitap7ltwebproj1.entity.ProductImage;

@Component
public class ProductImageMapper {

    public ProductImageDTO toDTO(ProductImage entity) {
        if (entity == null) {
            return null;
        }
        return ProductImageDTO.builder()
                .id(entity.getId())
                .imageUrl(entity.getImageUrl())
                .primary(entity.getPrimary())
                .displayOrder(entity.getDisplayOrder())
                .build();
    }
}
