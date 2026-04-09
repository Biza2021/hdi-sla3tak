package com.hdisla3tak.app.service;

import com.hdisla3tak.app.domain.enums.RepairStatus;
import com.hdisla3tak.app.repository.CustomerRepository;
import com.hdisla3tak.app.repository.RepairItemRepository;
import com.hdisla3tak.app.tenant.ShopContext;
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
    private final ShopContext shopContext;

    public DashboardService(CustomerRepository customerRepository,
                            RepairItemRepository repairItemRepository,
                            MessageSource messageSource,
                            ShopContext shopContext) {
        this.customerRepository = customerRepository;
        this.repairItemRepository = repairItemRepository;
        this.messageSource = messageSource;
        this.shopContext = shopContext;
    }

    public Map<String, Long> getStats(Locale locale) {
        Long shopId = shopContext.requireCurrentShop().getId();
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put(messageSource.getMessage("dashboard.stats.totalCustomers", null, locale), customerRepository.countByShop_Id(shopId));
        stats.put(messageSource.getMessage("dashboard.stats.totalItems", null, locale), repairItemRepository.countByShop_Id(shopId));
        stats.put(messageSource.getMessage("dashboard.stats.receivedToday", null, locale), repairItemRepository.countByDateReceivedAndShop_Id(LocalDate.now(), shopId));
        stats.put(messageSource.getMessage("dashboard.stats.underRepair", null, locale), repairItemRepository.countByStatusAndShop_Id(RepairStatus.UNDER_REPAIR, shopId));
        stats.put(messageSource.getMessage("dashboard.stats.readyForPickup", null, locale), repairItemRepository.countByStatusAndShop_Id(RepairStatus.READY_FOR_PICKUP, shopId));
        stats.put(messageSource.getMessage("dashboard.stats.delivered", null, locale), repairItemRepository.countByDeliveredAtIsNotNullAndShop_Id(shopId));
        stats.put(messageSource.getMessage("dashboard.stats.overdue", null, locale), repairItemRepository.countByExpectedDeliveryDateBeforeAndDeliveredAtIsNullAndShop_Id(LocalDate.now(), shopId));
        return stats;
    }
}
