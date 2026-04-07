package com.hdisla3tak.app.repository;

import com.hdisla3tak.app.domain.ShopSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShopSettingsRepository extends JpaRepository<ShopSettings, Long> {

    Optional<ShopSettings> findTopByOrderByIdAsc();
}
