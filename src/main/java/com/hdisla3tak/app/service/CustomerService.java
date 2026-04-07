package com.hdisla3tak.app.service;

import com.hdisla3tak.app.domain.Customer;
import com.hdisla3tak.app.repository.CustomerRepository;
import com.hdisla3tak.app.web.form.CustomerForm;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> findAll(String q) {
        if (!StringUtils.hasText(q)) {
            return customerRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();
        }
        return customerRepository.findByFullNameContainingIgnoreCaseOrPhoneNumberContainingIgnoreCaseOrderByCreatedAtDesc(q, q);
    }

    public Customer getById(Long id) {
        return customerRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Customer not found."));
    }

    public Customer create(CustomerForm form) {
        validatePhone(form.getPhoneNumber());
        if (customerRepository.existsByPhoneNumber(form.getPhoneNumber().trim())) {
            throw new IllegalArgumentException("duplicate-phone");
        }
        Customer customer = new Customer();
        apply(customer, form);
        return customerRepository.save(customer);
    }

    public Customer update(Long id, CustomerForm form) {
        validatePhone(form.getPhoneNumber());
        if (customerRepository.existsByPhoneNumberAndIdNot(form.getPhoneNumber().trim(), id)) {
            throw new IllegalArgumentException("duplicate-phone");
        }
        Customer customer = getById(id);
        apply(customer, form);
        return customerRepository.save(customer);
    }

    private void apply(Customer customer, CustomerForm form) {
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

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
