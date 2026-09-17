package org.example.baitap7ltwebproj1.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.baitap7ltwebproj1.dto.ProductDTO;
import org.example.baitap7ltwebproj1.entity.Product;
import org.example.baitap7ltwebproj1.entity.ProductImage;
import org.example.baitap7ltwebproj1.mapper.ProductMapper;
import org.example.baitap7ltwebproj1.repository.ProductImageRepository;
import org.example.baitap7ltwebproj1.repository.ProductRepository;
import org.example.baitap7ltwebproj1.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductImageRepository imageRepository;

    private final Path uploadDir = Paths.get("uploads/products");

    // =====================================================
    // FIND ALL — không phân trang (dùng cho REST API)
    // =====================================================
    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> findAll() {
        return productRepository.findAll(Sort.by("id").ascending())
                .stream()
                .map(productMapper::toDTO)
                .toList();
    }

    // =====================================================
    // SEARCH + PAGINATION
    // =====================================================
    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findAll(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Page<Product> products;
        if (keyword == null || keyword.isBlank()) {
            products = productRepository.findAll(pageable);
        } else {
            products = productRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);
        }

        return products.map(productMapper::toDTO);
    }

    // =====================================================
    // FIND BY ID
    // =====================================================
    @Override
    @Transactional(readOnly = true)
    public ProductDTO findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + id));
        return productMapper.toDTO(product);
    }

    // =====================================================
    // CREATE
    // =====================================================
    @Override
    public ProductDTO create(ProductDTO dto) {
        try {
            // DTO -> Entity
            Product product = productMapper.toEntity(dto);

            // Lưu Product trước để có ID
            Product saved = productRepository.save(product);

            // Upload và gắn nhiều ảnh
            saveImages(saved, dto.getImageFiles());

            // Lưu lại sau khi gắn ảnh
            Product result = productRepository.save(saved);

            return productMapper.toDTO(result);
        } catch (IOException e) {
            throw new RuntimeException("Không thể upload hình ảnh: " + e.getMessage(), e);
        }
    }

    // =====================================================
    // UPDATE
    // =====================================================
    @Override
    public ProductDTO update(Long id, ProductDTO dto) {
        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + id));

            // Cập nhật các trường cơ bản
            productMapper.updateEntity(dto, product);

            // Upload thêm ảnh mới (nếu có)
            saveImages(product, dto.getImageFiles());

            Product updated = productRepository.save(product);
            return productMapper.toDTO(updated);
        } catch (IOException e) {
            throw new RuntimeException("Không thể upload hình ảnh: " + e.getMessage(), e);
        }
    }

    // =====================================================
    // DELETE PRODUCT (cascade xóa tất cả ProductImage)
    // =====================================================
    @Override
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + id));

        // Xóa file vật lý của từng ảnh
        if (product.getImages() != null) {
            for (ProductImage image : product.getImages()) {
                deleteFile(image.getImageUrl());
            }
        }

        // CascadeType.ALL + orphanRemoval = true => ProductImage tự xóa theo
        productRepository.delete(product);
    }

    // =====================================================
    // SAVE ONE IMAGE — trả về tên file (UUID.ext) hoặc null
    // =====================================================
    @Override
    public String saveImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        Files.createDirectories(uploadDir);

        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        }

        String fileName = UUID.randomUUID().toString() + extension;
        Path target = uploadDir.resolve(fileName);

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }

    // =====================================================
    // SAVE MULTIPLE IMAGES — gắn vào collection của Product
    // =====================================================
    @Override
    public void saveImages(Product product, List<MultipartFile> files) throws IOException {
        if (files == null || files.isEmpty()) {
            return;
        }

        int currentOrder = product.getImages().size();

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            String fileName = saveImage(file);

            // Ảnh đầu tiên của sản phẩm (chưa có ảnh nào) thì làm ảnh chính
            boolean isPrimary = product.getImages().isEmpty();

            ProductImage image = ProductImage.builder()
                    .product(product)
                    .imageUrl(fileName)
                    .primary(isPrimary)
                    .displayOrder(currentOrder++)
                    .createdAt(LocalDateTime.now())
                    .build();

            product.getImages().add(image);
        }
    }

    // =====================================================
    // DELETE ONE IMAGE (xóa file + record + cập nhật ảnh chính)
    // =====================================================
    @Override
    public Long deleteImage(Long imageId) {
        ProductImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hình ảnh: " + imageId));

        Product product = image.getProduct();
        Long productId = product.getId();
        boolean wasPrimary = Boolean.TRUE.equals(image.getPrimary());

        // Xóa file vật lý
        deleteFile(image.getImageUrl());

        // Xóa khỏi collection (orphanRemoval sẽ xóa DB)
        product.getImages().remove(image);
        imageRepository.delete(image);

        // Nếu vừa xóa ảnh chính thì chỉ định ảnh đầu tiên còn lại làm ảnh chính
        if (wasPrimary && !product.getImages().isEmpty()) {
            ProductImage newPrimary = product.getImages().get(0);
            newPrimary.setPrimary(true);
            newPrimary.setDisplayOrder(0);
        }

        return productId;
    }

    // =====================================================
    // DELETE PHYSICAL FILE (chống Path Traversal)
    // =====================================================
    @Override
    public void deleteFile(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return;
        }
        try {
            Path uploadRoot = uploadDir.toAbsolutePath().normalize();
            Path file = uploadRoot.resolve(fileName).normalize();

            // Bảo vệ khỏi path traversal attack
            if (!file.startsWith(uploadRoot)) {
                throw new SecurityException("Tên file không hợp lệ: " + fileName);
            }

            Files.deleteIfExists(file);
        } catch (IOException e) {
            System.err.println("Không thể xóa file: " + fileName + " — " + e.getMessage());
        }
    }
}
