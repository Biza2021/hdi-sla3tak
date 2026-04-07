package com.hdisla3tak.app.service;

import com.hdisla3tak.app.domain.enums.RepairStatus;
import com.hdisla3tak.app.repository.CustomerRepository;
import com.hdisla3tak.app.repository.RepairItemRepository;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class DashboardService {

    private final CustomerRepository customerRepository;
    private final RepairItemRepository repairItemRepository;
    private final MessageSource messageSource;

    public DashboardService(CustomerRepository customerRepository,
                            RepairItemRepository repairItemRepository,
                            MessageSource messageSource) {
        this.customerRepository = customerRepository;
        this.repairItemRepository = repairItemRepository;
        this.messageSource = messageSource;
    }

    public Map<String, Long> getStats(Locale locale) {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put(messageSource.getMessage("dashboard.stats.totalCustomers", null, locale), customerRepository.count());
        stats.put(messageSource.getMessage("dashboard.stats.totalItems", null, locale), repairItemRepository.count());
        stats.put(messageSource.getMessage("dashboard.stats.receivedToday", null, locale), repairItemRepository.countByDateReceived(LocalDate.now()));
        stats.put(messageSource.getMessage("dashboard.stats.underRepair", null, locale), repairItemRepository.countByStatus(RepairStatus.UNDER_REPAIR));
        stats.put(messageSource.getMessage("dashboard.stats.readyForPickup", null, locale), repairItemRepository.countByStatus(RepairStatus.READY_FOR_PICKUP));
        stats.put(messageSource.getMessage("dashboard.stats.delivered", null, locale), repairItemRepository.countByDeliveredAtIsNotNull());
        stats.put(messageSource.getMessage("dashboard.stats.overdue", null, locale), repairItemRepository.countByExpectedDeliveryDateBeforeAndDeliveredAtIsNull(LocalDate.now()));
        return stats;
    }
}
