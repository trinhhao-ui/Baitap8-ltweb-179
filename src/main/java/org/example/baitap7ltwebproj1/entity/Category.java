package org.example.baitap7ltwebproj1.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200, columnDefinition = "NVARCHAR(200)", unique = true)
    private String name;

    @Column(length = 500, columnDefinition = "NVARCHAR(500)")
    private String description;

    @Column(length = 500, columnDefinition = "NVARCHAR(500)")
    private String icon;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // =====================================
    // CATEGORY 1 ---- N PRODUCT
    // =====================================
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Product> products = new ArrayList<>();
}
