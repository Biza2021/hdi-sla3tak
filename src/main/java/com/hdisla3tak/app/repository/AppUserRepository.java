package com.hdisla3tak.app.repository;

import com.hdisla3tak.app.domain.AppUser;
import com.hdisla3tak.app.domain.Shop;
import com.hdisla3tak.app.domain.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsernameIgnoreCase(String username);
    boolean existsByUsernameIgnoreCase(String username);
    Optional<AppUser> findByUsernameIgnoreCaseAndShop_SlugIgnoreCase(String username, String shopSlug);
    Optional<AppUser> findByUsernameIgnoreCaseAndShop_Id(String username, Long shopId);
    boolean existsByUsernameIgnoreCaseAndShop_Id(String username, Long shopId);
    Optional<AppUser> findByIdAndShop_Id(Long id, Long shopId);
    List<AppUser> findAllByShop_IdOrderByCreatedAtDesc(Long shopId);
    long countByRoleAndShop_Id(UserRole role, Long shopId);
    long countByShopIsNull();

    @Modifying
    @Query("update AppUser u set u.shop = :shop where u.shop is null")
    int assignShopWhereNull(@Param("shop") Shop shop);
}
