package com.hdisla3tak.app.service;

import com.hdisla3tak.app.domain.ShopSettings;
import com.hdisla3tak.app.repository.ShopSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ShopSettingsService {

    public static final String DEFAULT_BUSINESS_NAME = "Hdi Sla3tak";

    private final ShopSettingsRepository shopSettingsRepository;

    public ShopSettingsService(ShopSettingsRepository shopSettingsRepository) {
        this.shopSettingsRepository = shopSettingsRepository;
    }

    @Transactional(readOnly = true)
    public String getBusinessName() {
        return shopSettingsRepository.findTopByOrderByIdAsc()
            .map(ShopSettings::getBusinessName)
            .filter(StringUtils::hasText)
            .map(String::trim)
            .orElse(DEFAULT_BUSINESS_NAME);
    }

    @Transactional(readOnly = true)
    public String getEditableBusinessName() {
        return shopSettingsRepository.findTopByOrderByIdAsc()
            .map(ShopSettings::getBusinessName)
            .filter(StringUtils::hasText)
            .map(String::trim)
            .orElse(DEFAULT_BUSINESS_NAME);
    }

    @Transactional
    public void saveBusinessName(String businessName) {
        ShopSettings settings = shopSettingsRepository.findTopByOrderByIdAsc()
            .orElseGet(ShopSettings::new);
        settings.setBusinessName(businessName.trim());
        shopSettingsRepository.save(settings);
    }
}
