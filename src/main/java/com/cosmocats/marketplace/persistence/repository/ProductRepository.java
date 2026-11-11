package com.cosmocats.marketplace.persistence.repository;

import com.cosmocats.marketplace.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
}
