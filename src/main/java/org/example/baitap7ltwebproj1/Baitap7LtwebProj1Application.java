package org.example.baitap7ltwebproj1;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.web.filter.CharacterEncodingFilter;

@SpringBootApplication
public class Baitap7LtwebProj1Application {

    public static void main(String[] args) {
        SpringApplication.run(Baitap7LtwebProj1Application.class, args);
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    CharacterEncodingFilter characterEncodingFilter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        return filter;
    }

    /** In link ra terminal IntelliJ khi ứng dụng khởi động xong */
    @Bean
    ApplicationRunner printLinks(Environment env) {
        return (ApplicationArguments args) -> {
            String port = env.getProperty("server.port", "8080");
            String line = "=".repeat(60);
            System.out.println("\n" + line);
            System.out.println("  ✅ Ứng dụng đã khởi động thành công!");
            System.out.println(line);
            System.out.println("  🌐 Trang chủ      : http://localhost:" + port + "/products");
            System.out.println("  ⚡ Ajax Product   : http://localhost:" + port + "/products/ajax-product");
            System.out.println("  🗂  Ajax Category  : http://localhost:" + port + "/products/ajax-category");
            System.out.println(line + "\n");
        };
    }
}
