package com.hdisla3tak.app.repository;

import com.hdisla3tak.app.domain.RepairItem;
import com.hdisla3tak.app.domain.enums.RepairStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RepairItemRepository extends JpaRepository<RepairItem, Long>, JpaSpecificationExecutor<RepairItem> {
    boolean existsByPickupCode(String pickupCode);
    boolean existsByPublicTrackingToken(String publicTrackingToken);
    Optional<RepairItem> findByPublicTrackingToken(String publicTrackingToken);
    long countByStatus(RepairStatus status);
    long countByDateReceived(LocalDate dateReceived);
    long countByDeliveredAtIsNotNull();
    long countByExpectedDeliveryDateBeforeAndDeliveredAtIsNull(LocalDate date);

    @Query("""
        select distinct r from RepairItem r
        join fetch r.customer c
        where lower(r.pickupCode) like lower(concat('%', :q, '%'))
           or lower(c.phoneNumber) like lower(concat('%', :q, '%'))
           or lower(c.fullName) like lower(concat('%', :q, '%'))
        order by r.updatedAt desc
        """)
    List<RepairItem> searchForDelivery(@Param("q") String q);
}
