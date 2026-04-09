package com.hdisla3tak.app.repository;

import com.hdisla3tak.app.domain.RepairItem;
import com.hdisla3tak.app.domain.Shop;
import com.hdisla3tak.app.domain.enums.RepairStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RepairItemRepository extends JpaRepository<RepairItem, Long>, JpaSpecificationExecutor<RepairItem> {

    @Override
    @EntityGraph(attributePaths = "customer")
    List<RepairItem> findAll(@Nullable Specification<RepairItem> spec, Sort sort);

    @Override
    @EntityGraph(attributePaths = {"customer", "shop"})
    Optional<RepairItem> findById(Long id);

    boolean existsByPickupCode(String pickupCode);
    boolean existsByPickupCodeAndShop_Id(String pickupCode, Long shopId);
    boolean existsByPublicTrackingToken(String publicTrackingToken);
    @EntityGraph(attributePaths = {"customer", "shop"})
    Optional<RepairItem> findByPublicTrackingToken(String publicTrackingToken);
    long countByShopIsNull();
    long countByShop_Id(Long shopId);
    long countByStatus(RepairStatus status);
    long countByStatusAndShop_Id(RepairStatus status, Long shopId);
    long countByDateReceived(LocalDate dateReceived);
    long countByDateReceivedAndShop_Id(LocalDate dateReceived, Long shopId);
    long countByDeliveredAtIsNotNull();
    long countByDeliveredAtIsNotNullAndShop_Id(Long shopId);
    long countByExpectedDeliveryDateBeforeAndDeliveredAtIsNull(LocalDate date);
    long countByExpectedDeliveryDateBeforeAndDeliveredAtIsNullAndShop_Id(LocalDate date, Long shopId);
    @EntityGraph(attributePaths = {"customer", "shop"})
    Optional<RepairItem> findByIdAndShop_Id(Long id, Long shopId);

    @Query("""
        select distinct r from RepairItem r
        join fetch r.customer c
        where lower(r.pickupCode) like lower(concat('%', :q, '%'))
           or lower(c.phoneNumber) like lower(concat('%', :q, '%'))
           or lower(c.fullName) like lower(concat('%', :q, '%'))
        order by r.updatedAt desc
        """)
    List<RepairItem> searchForDelivery(@Param("q") String q);

    @Query("""
        select distinct r from RepairItem r
        join fetch r.customer c
        where r.shop.id = :shopId
          and (
            lower(r.pickupCode) like lower(concat('%', :q, '%'))
            or lower(c.phoneNumber) like lower(concat('%', :q, '%'))
            or lower(c.fullName) like lower(concat('%', :q, '%'))
          )
        order by r.updatedAt desc
        """)
    List<RepairItem> searchForDeliveryByShopId(@Param("shopId") Long shopId, @Param("q") String q);

    @Modifying
    @Query("update RepairItem r set r.shop = :shop where r.shop is null")
    int assignShopWhereNull(@Param("shop") Shop shop);
}
