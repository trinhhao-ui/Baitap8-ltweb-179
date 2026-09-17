package org.example.baitap7ltwebproj1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.baitap7ltwebproj1.dto.ProductDTO;
import org.example.baitap7ltwebproj1.service.CategoryService;
import org.example.baitap7ltwebproj1.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService  productService;
    private final CategoryService categoryService;

    // =====================================================
    // LIST + SEARCH + PAGINATION
    // =====================================================
    @GetMapping
    public String listProducts(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        Page<ProductDTO> productPage = productService.findAll(keyword, page, size);

        int totalPages  = productPage.getTotalPages();
        int currentPage = productPage.getNumber();
        int startPage   = Math.max(0, currentPage - 2);
        int endPage     = Math.min(totalPages - 1, startPage + 4);
        if (endPage - startPage < 4) startPage = Math.max(0, endPage - 4);

        model.addAttribute("products",  productPage);
        model.addAttribute("keyword",   keyword);
        model.addAttribute("size",      size);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage",   endPage);

        return "products/list";
    }

    // =====================================================
    // CREATE FORM
    // =====================================================
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("product",    new ProductDTO());
        model.addAttribute("formTitle",  "Thêm sản phẩm");
        model.addAttribute("categories", categoryService.findAll());
        return "products/form";
    }

    // =====================================================
    // CREATE — multipart/form-data
    // =====================================================
    @PostMapping(value = "/create", consumes = "multipart/form-data")
    public String create(
            @Valid @ModelAttribute("product") ProductDTO dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("formTitle",  "Thêm sản phẩm");
            model.addAttribute("categories", categoryService.findAll());
            return "products/form";
        }
        try {
            productService.create(dto);
            redirectAttributes.addFlashAttribute("success", "Thêm sản phẩm thành công");
            return "redirect:/products";
        } catch (Exception e) {
            model.addAttribute("formTitle",  "Thêm sản phẩm");
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("error", "Không thể thêm sản phẩm: " + e.getMessage());
            return "products/form";
        }
    }

    // =====================================================
    // EDIT FORM
    // =====================================================
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("product",    productService.findById(id));
        model.addAttribute("formTitle",  "Cập nhật sản phẩm");
        model.addAttribute("categories", categoryService.findAll());
        return "products/form";
    }

    // =====================================================
    // UPDATE — multipart/form-data
    // =====================================================
    @PostMapping(value = "/edit/{id}", consumes = "multipart/form-data")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("product") ProductDTO dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("formTitle",  "Cập nhật sản phẩm");
            model.addAttribute("categories", categoryService.findAll());
            return "products/form";
        }
        try {
            productService.update(id, dto);
            redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm thành công");
            return "redirect:/products";
        } catch (Exception e) {
            model.addAttribute("formTitle",  "Cập nhật sản phẩm");
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("error", "Không thể cập nhật sản phẩm: " + e.getMessage());
            return "products/form";
        }
    }

    // =====================================================
    // DELETE PRODUCT
    // =====================================================
    @GetMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            productService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Xóa sản phẩm thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa sản phẩm: " + e.getMessage());
        }
        return "redirect:/products";
    }

    // =====================================================
    // AJAX CRUD PAGE — Product
    // =====================================================
    @GetMapping("/ajax-product")
    public String ajaxProduct() {
        return "products/ajax-product";
    }

    // =====================================================
    // AJAX CRUD PAGE — Category
    // =====================================================
    @GetMapping("/ajax-category")
    public String ajaxCategory() {
        return "products/ajax-category";
    }

    // =====================================================
    // AJAX CRUD PAGE (cũ — giữ tương thích)
    // =====================================================
    @GetMapping("/ajax")
    public String ajaxList() {
        return "redirect:/products/ajax-product";
    }

    // =====================================================
    // DELETE ONE PRODUCT IMAGE (fetch POST)
    // =====================================================
    @PostMapping("/image/delete/{imageId}")
    public String deleteImage(
            @PathVariable Long imageId,
            RedirectAttributes redirectAttributes) {
        try {
            Long productId = productService.deleteImage(imageId);
            redirectAttributes.addFlashAttribute("success", "Xóa hình ảnh thành công");
            return "redirect:/products/edit/" + productId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa hình ảnh: " + e.getMessage());
            return "redirect:/products";
        }
    }
}
