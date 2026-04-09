package com.hdisla3tak.app.repository;

import com.hdisla3tak.app.domain.Customer;
import com.hdisla3tak.app.domain.Shop;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    interface CustomerRepairCount {
        Long getCustomerId();
        long getRepairCount();
    }

    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long id);
    long countByShopIsNull();
    long countByShop_Id(Long shopId);
    List<Customer> findAllByShop_Id(Long shopId);
    List<Customer> findAllByShop_IdOrderByCreatedAtDesc(Long shopId);
    @Query("select c from Customer c where c.shop.id = :shopId order by lower(c.fullName) asc, c.createdAt desc")
    List<Customer> findAllForSelectionByShopId(@Param("shopId") Long shopId);
    List<Customer> findAllByOrderByCreatedAtDesc();
    List<Customer> findByFullNameContainingIgnoreCaseOrPhoneNumberContainingIgnoreCaseOrderByCreatedAtDesc(String fullName, String phoneNumber);
    @Query("""
        select c from Customer c
        where c.shop.id = :shopId
          and (
            lower(c.fullName) like lower(concat('%', :q, '%'))
            or lower(c.phoneNumber) like lower(concat('%', :q, '%'))
          )
        order by c.createdAt desc
        """)
    List<Customer> searchByShopId(@Param("shopId") Long shopId, @Param("q") String q);
    Optional<Customer> findByIdAndShop_Id(Long id, Long shopId);

    @EntityGraph(attributePaths = "repairItems")
    @Query("select c from Customer c where c.id = :id")
    Optional<Customer> findDetailById(@Param("id") Long id);

    @EntityGraph(attributePaths = "repairItems")
    @Query("select c from Customer c where c.id = :id and c.shop.id = :shopId")
    Optional<Customer> findDetailByIdAndShopId(@Param("id") Long id, @Param("shopId") Long shopId);

    @Query("""
        select c.id as customerId, count(r) as repairCount
        from Customer c
        left join c.repairItems r
        where c.id in :customerIds
        group by c.id
        """)
    List<CustomerRepairCount> countRepairsByCustomerIds(@Param("customerIds") List<Long> customerIds);

    @Query("""
        select c.id as customerId, count(r) as repairCount
        from Customer c
        left join c.repairItems r
        where c.shop.id = :shopId and c.id in :customerIds
        group by c.id
        """)
    List<CustomerRepairCount> countRepairsByShopIdAndCustomerIds(@Param("shopId") Long shopId,
                                                                 @Param("customerIds") List<Long> customerIds);

    @Modifying
    @Query("update Customer c set c.shop = :shop where c.shop is null")
    int assignShopWhereNull(@Param("shop") Shop shop);
}
