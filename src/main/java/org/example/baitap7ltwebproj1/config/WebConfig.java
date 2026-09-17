package org.example.baitap7ltwebproj1.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Ánh xạ URL /uploads/** -> thư mục uploads/ trong project
 * Ví dụ: /uploads/products/abc.jpg -> file:uploads/products/abc.jpg
 * Truy cập: http://localhost:8099/uploads/products/abc.jpg
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
