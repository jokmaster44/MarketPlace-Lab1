package com.cosmocats.marketplace.service;

import com.cosmocats.marketplace.domain.Category;
import com.cosmocats.marketplace.persistence.entity.CategoryEntity;
import com.cosmocats.marketplace.persistence.repository.CategoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CategoryService {
    private final CategoryRepository categories;

    public CategoryService(CategoryRepository categories) {
        this.categories = categories;
    }

    public Category create(Category category) {
        return toDomain(categories.save(new CategoryEntity(category.name())));
    }

    @Transactional(readOnly = true)
    public Category findById(long id) {
        return toDomain(categories.findById(id).orElseThrow(() -> new CategoryNotFoundException(id)));
    }

    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categories.findAll().stream().map(this::toDomain).toList();
    }

    public Category update(long id, Category category) {
        CategoryEntity entity = categories.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        entity.setName(category.name());
        return toDomain(categories.save(entity));
    }

    public void delete(long id) {
        CategoryEntity entity = categories.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        categories.delete(entity);
    }

    private Category toDomain(CategoryEntity entity) {
        return new Category(entity.getId(), entity.getName());
    }
}
