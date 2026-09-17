package org.example.baitap7ltwebproj1.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.baitap7ltwebproj1.dto.CategoryDTO;
import org.example.baitap7ltwebproj1.entity.Category;
import org.example.baitap7ltwebproj1.repository.CategoryRepository;
import org.example.baitap7ltwebproj1.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    private final Path uploadDir = Paths.get("uploads/categories");

    // =====================================================
    // FIND ALL — không phân trang (dùng cho dropdown)
    // =====================================================
    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> findAll() {
        return categoryRepository.findAll(Sort.by("id").ascending())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // =====================================================
    // FIND ALL — tìm kiếm + phân trang (dùng cho Ajax table)
    // =====================================================
    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDTO> findAll(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        if (keyword == null || keyword.isBlank()) {
            return categoryRepository.findAll(pageable).map(this::toDTO);
        }
        return categoryRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable).map(this::toDTO);
    }

    // =====================================================
    // FIND BY ID
    // =====================================================
    @Override
    @Transactional(readOnly = true)
    public CategoryDTO findById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục id=" + id));
        return toDTO(category);
    }

    // =====================================================
    // CREATE
    // =====================================================
    @Override
    public CategoryDTO create(CategoryDTO dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new RuntimeException("Danh mục \"" + dto.getName() + "\" đã tồn tại");
        }
        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setCreatedAt(LocalDateTime.now());
        category.setIcon(saveIcon(dto.getIconFile()));
        return toDTO(categoryRepository.save(category));
    }

    // =====================================================
    // UPDATE
    // =====================================================
    @Override
    public CategoryDTO update(Long id, CategoryDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục id=" + id));

        // Kiểm tra trùng tên với category khác
        categoryRepository.findByName(dto.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new RuntimeException("Tên danh mục \"" + dto.getName() + "\" đã được sử dụng");
            }
        });

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());

        // Cập nhật icon nếu có file mới
        String newIcon = saveIcon(dto.getIconFile());
        if (newIcon != null) {
            category.setIcon(newIcon);
        }

        return toDTO(categoryRepository.save(category));
    }

    // =====================================================
    // DELETE
    // =====================================================
    @Override
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục id=" + id));
        categoryRepository.delete(category);
    }

    // =====================================================
    // EXISTS BY NAME
    // =====================================================
    @Override
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }

    // =====================================================
    // HELPER: lưu file icon, trả về tên file hoặc null
    // =====================================================
    private String saveIcon(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        try {
            Files.createDirectories(uploadDir);
            String original = file.getOriginalFilename();
            String ext = (original != null && original.contains("."))
                    ? original.substring(original.lastIndexOf(".")).toLowerCase() : "";
            String fileName = UUID.randomUUID() + ext;
            Files.copy(file.getInputStream(),
                    uploadDir.resolve(fileName),
                    StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu icon: " + e.getMessage(), e);
        }
    }

    // =====================================================
    // HELPER: Entity → DTO
    // =====================================================
    private CategoryDTO toDTO(Category c) {
        return CategoryDTO.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .icon(c.getIcon())
                .build();
    }
}
