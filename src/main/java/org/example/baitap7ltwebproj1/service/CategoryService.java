package org.example.baitap7ltwebproj1.service;

import org.example.baitap7ltwebproj1.dto.CategoryDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CategoryService {

    /** Lấy tất cả (dùng cho dropdown) */
    List<CategoryDTO> findAll();

    /** Tìm kiếm + phân trang (dùng cho Ajax table) */
    Page<CategoryDTO> findAll(String keyword, int page, int size);

    CategoryDTO findById(Long id);

    CategoryDTO create(CategoryDTO dto);

    CategoryDTO update(Long id, CategoryDTO dto);

    void delete(Long id);

    boolean existsByName(String name);
}
