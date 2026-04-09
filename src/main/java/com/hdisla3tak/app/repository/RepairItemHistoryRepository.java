package com.hdisla3tak.app.repository;

import com.hdisla3tak.app.domain.RepairItemHistory;
import com.hdisla3tak.app.domain.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RepairItemHistoryRepository extends JpaRepository<RepairItemHistory, Long> {

    long countByShopIsNull();

    @Modifying
    @Query("update RepairItemHistory h set h.shop = :shop where h.shop is null")
    int assignShopWhereNull(@Param("shop") Shop shop);
}
