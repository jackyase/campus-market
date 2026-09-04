package org.example.demo4.config;

import org.example.demo4.product.Category;
import org.example.demo4.product.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initCategories(CategoryRepository categoryRepository) {
        return args -> {
            if (categoryRepository.count() > 0) {
                return;
            }
            List<String> names = List.of(
                    "教材书籍", "数码电子", "生活用品", "运动户外",
                    "服饰鞋包", "美妆个护", "交通工具", "其他");
            for (int i = 0; i < names.size(); i++) {
                Category c = new Category();
                c.setName(names.get(i));
                c.setSortOrder(i);
                categoryRepository.save(c);
            }
        };
    }
}
