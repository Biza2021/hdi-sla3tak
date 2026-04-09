package com.hdisla3tak.app.service;

import com.hdisla3tak.app.domain.ShopSettings;
import com.hdisla3tak.app.repository.ShopRepository;
import com.hdisla3tak.app.repository.ShopSettingsRepository;
import com.hdisla3tak.app.tenant.ShopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ShopSettingsService {

    public static final String DEFAULT_BUSINESS_NAME = "Hdi Sla3tak";

    private final ShopSettingsRepository shopSettingsRepository;
    private final ShopRepository shopRepository;
    private final ShopContext shopContext;
    private volatile String cachedBusinessName;

    public ShopSettingsService(ShopSettingsRepository shopSettingsRepository,
                               ShopRepository shopRepository,
                               ShopContext shopContext) {
        this.shopSettingsRepository = shopSettingsRepository;
        this.shopRepository = shopRepository;
        this.shopContext = shopContext;
    }

    @Transactional(readOnly = true)
    public String getBusinessName() {
        String currentShopName = shopContext.getCurrentShop()
            .map(shop -> StringUtils.hasText(shop.getName()) ? shop.getName().trim() : DEFAULT_BUSINESS_NAME)
            .orElseGet(this::getSingleShopName);
        if (currentShopName != null) {
            return currentShopName;
        }

        String cached = cachedBusinessName;
        if (cached != null) {
            return cached;
        }

        String resolvedBusinessName = DEFAULT_BUSINESS_NAME;
        cachedBusinessName = resolvedBusinessName;
        return resolvedBusinessName;
    }

    @Transactional(readOnly = true)
    public String getEditableBusinessName() {
        return shopContext.getCurrentShop()
            .map(shop -> StringUtils.hasText(shop.getName()) ? shop.getName().trim() : DEFAULT_BUSINESS_NAME)
            .orElseGet(() -> {
                String singleShopName = getSingleShopName();
                return singleShopName != null ? singleShopName : getBusinessName();
            });
    }

    @Transactional
    public void saveBusinessName(String businessName) {
        String trimmedBusinessName = businessName.trim();
        List<com.hdisla3tak.app.domain.Shop> shops = shopRepository.findAllByOrderByCreatedAtAsc();
        shopContext.getCurrentShop().ifPresentOrElse(
            shop -> {
                if (!trimmedBusinessName.equals(shop.getName())) {
                    shop.setName(trimmedBusinessName);
                    shopRepository.save(shop);
                }
            },
            () -> {
                if (shops.size() == 1) {
                    com.hdisla3tak.app.domain.Shop shop = shops.get(0);
                    if (!trimmedBusinessName.equals(shop.getName())) {
                        shop.setName(trimmedBusinessName);
                        shopRepository.save(shop);
                    }
                    return;
                }
                saveLegacyFallbackBusinessName(trimmedBusinessName);
            }
        );
        cachedBusinessName = trimmedBusinessName;
    }

    private void saveLegacyFallbackBusinessName(String businessName) {
        ShopSettings settings = shopSettingsRepository.findTopByOrderByIdAsc()
            .orElseGet(ShopSettings::new);
        settings.setBusinessName(businessName);
        shopSettingsRepository.save(settings);
    }

    private String getSingleShopName() {
        List<com.hdisla3tak.app.domain.Shop> shops = shopRepository.findAllByOrderByCreatedAtAsc();
        if (shops.size() != 1) {
            return null;
        }
        String shopName = shops.get(0).getName();
        return StringUtils.hasText(shopName) ? shopName.trim() : DEFAULT_BUSINESS_NAME;
    }
}
