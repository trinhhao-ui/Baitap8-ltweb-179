package org.example.baitap7ltwebproj1.service;

import org.example.baitap7ltwebproj1.dto.ProductDTO;
import org.example.baitap7ltwebproj1.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ProductService {

    Page<ProductDTO> findAll(String keyword, int page, int size);

    /** Lấy toàn bộ sản phẩm (dùng cho REST API / Ajax) */
    List<ProductDTO> findAll();

    ProductDTO findById(Long id);

    ProductDTO create(ProductDTO dto);

    ProductDTO update(Long id, ProductDTO dto);

    void delete(Long id);

    String saveImage(MultipartFile file) throws IOException;

    void saveImages(Product product, List<MultipartFile> files) throws IOException;

    Long deleteImage(Long imageId);

    void deleteFile(String fileName);
}
