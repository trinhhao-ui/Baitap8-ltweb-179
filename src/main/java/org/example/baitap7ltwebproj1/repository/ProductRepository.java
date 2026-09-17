package org.example.baitap7ltwebproj1.repository;

import org.example.baitap7ltwebproj1.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Tìm kiếm sản phẩm theo tên, không phân biệt hoa thường, có hỗ trợ phân trang
     * GET /products?keyword=laptop&page=0&size=5
     */
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
