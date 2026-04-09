package com.hdisla3tak.app.repository;

import com.hdisla3tak.app.domain.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Long> {

    Optional<Shop> findBySlugIgnoreCase(String slug);
    boolean existsBySlugIgnoreCase(String slug);
    List<Shop> findAllByOrderByCreatedAtAsc();
}
