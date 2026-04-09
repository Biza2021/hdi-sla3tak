package com.hdisla3tak.app.service;

import com.hdisla3tak.app.domain.Customer;
import com.hdisla3tak.app.repository.CustomerRepository;
import com.hdisla3tak.app.tenant.ShopContext;
import com.hdisla3tak.app.web.form.CustomerForm;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final ShopContext shopContext;

    public CustomerService(CustomerRepository customerRepository,
                           ShopContext shopContext) {
        this.customerRepository = customerRepository;
        this.shopContext = shopContext;
    }

    public List<Customer> findAll(String q) {
        Long shopId = currentShopId();
        if (!StringUtils.hasText(q)) {
            return customerRepository.findAllByShop_IdOrderByCreatedAtDesc(shopId);
        }
        return customerRepository.searchByShopId(shopId, q.trim());
    }

    public List<Customer> findAllForSelection() {
        return customerRepository.findAllForSelectionByShopId(currentShopId());
    }

    public Customer getById(Long id) {
        return customerRepository.findByIdAndShop_Id(id, currentShopId())
            .orElseThrow(() -> notFound());
    }

    public Customer getDetailById(Long id) {
        return customerRepository.findDetailByIdAndShopId(id, currentShopId())
            .orElseThrow(() -> notFound());
    }

    public Map<Long, Long> getRepairCounts(List<Customer> customers) {
        if (customers.isEmpty()) {
            return Map.of();
        }

        List<Long> customerIds = customers.stream()
            .map(Customer::getId)
            .toList();

        Map<Long, Long> repairCounts = new LinkedHashMap<>();
        for (Long customerId : customerIds) {
            repairCounts.put(customerId, 0L);
        }

        customerRepository.countRepairsByShopIdAndCustomerIds(currentShopId(), customerIds)
            .forEach(count -> repairCounts.put(count.getCustomerId(), count.getRepairCount()));

        return repairCounts;
    }

    public Customer create(CustomerForm form) {
        validatePhone(form.getPhoneNumber());
        String normalizedPhone = normalizePhone(form.getPhoneNumber());
        if (hasDuplicatePrimaryPhone(normalizedPhone, null)) {
            throw new IllegalArgumentException("duplicate-phone");
        }
        Customer customer = new Customer();
        apply(customer, form);
        return customerRepository.save(customer);
    }

    public Customer update(Long id, CustomerForm form) {
        validatePhone(form.getPhoneNumber());
        String normalizedPhone = normalizePhone(form.getPhoneNumber());
        if (hasDuplicatePrimaryPhone(normalizedPhone, id)) {
            throw new IllegalArgumentException("duplicate-phone");
        }
        Customer customer = getById(id);
        apply(customer, form);
        return customerRepository.save(customer);
    }

    private void apply(Customer customer, CustomerForm form) {
        customer.setShop(shopContext.requireCurrentShop());
        customer.setFullName(form.getFullName().trim());
        customer.setPhoneNumber(form.getPhoneNumber().trim());
        customer.setSecondaryPhoneNumber(trimToNull(form.getSecondaryPhoneNumber()));
        customer.setNotes(trimToNull(form.getNotes()));
    }

    private void validatePhone(String phone) {
        if (phone == null || !phone.matches("^[0-9()+ -]{8,20}$")) {
            throw new IllegalArgumentException("invalid-phone");
        }
    }

    private boolean hasDuplicatePrimaryPhone(String normalizedPhone, Long currentCustomerId) {
        return customerRepository.findAllByShop_Id(currentShopId()).stream()
            .filter(customer -> currentCustomerId == null || !customer.getId().equals(currentCustomerId))
            .map(Customer::getPhoneNumber)
            .map(this::normalizePhone)
            .anyMatch(normalizedPhone::equals);
    }

    private String normalizePhone(String phone) {
        return phone == null ? "" : phone.replaceAll("[()\\s-]", "");
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private Long currentShopId() {
        return shopContext.requireCurrentShop().getId();
    }

    private ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found.");
    }
}
