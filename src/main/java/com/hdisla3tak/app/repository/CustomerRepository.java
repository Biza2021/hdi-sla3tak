package com.hdisla3tak.app.repository;

import com.hdisla3tak.app.domain.Customer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
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
    List<Customer> findAllByOrderByCreatedAtDesc();
    List<Customer> findByFullNameContainingIgnoreCaseOrPhoneNumberContainingIgnoreCaseOrderByCreatedAtDesc(String fullName, String phoneNumber);

    @EntityGraph(attributePaths = "repairItems")
    @Query("select c from Customer c where c.id = :id")
    Optional<Customer> findDetailById(@Param("id") Long id);

    @Query("""
        select c.id as customerId, count(r) as repairCount
        from Customer c
        left join c.repairItems r
        where c.id in :customerIds
        group by c.id
        """)
    List<CustomerRepairCount> countRepairsByCustomerIds(@Param("customerIds") List<Long> customerIds);
}
