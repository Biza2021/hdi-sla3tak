package com.hdisla3tak.app.service;

import com.hdisla3tak.app.domain.Shop;
import com.hdisla3tak.app.domain.ShopSettings;
import com.hdisla3tak.app.repository.AppUserRepository;
import com.hdisla3tak.app.repository.CustomerRepository;
import com.hdisla3tak.app.repository.RepairItemHistoryRepository;
import com.hdisla3tak.app.repository.RepairItemRepository;
import com.hdisla3tak.app.repository.ShopRepository;
import com.hdisla3tak.app.repository.ShopSettingsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.List;
import java.util.Optional;

@Service
public class ShopService {

    public static final String DEFAULT_SHOP_SLUG = "default-shop";
    public static final String DEFAULT_SHOP_NAME = "Hdi Sla3tak";

    private static final Logger log = LoggerFactory.getLogger(ShopService.class);

    private final ShopRepository shopRepository;
    private final ShopSettingsRepository shopSettingsRepository;
    private final AppUserRepository appUserRepository;
    private final CustomerRepository customerRepository;
    private final RepairItemRepository repairItemRepository;
    private final RepairItemHistoryRepository repairItemHistoryRepository;

    public ShopService(ShopRepository shopRepository,
                       ShopSettingsRepository shopSettingsRepository,
                       AppUserRepository appUserRepository,
                       CustomerRepository customerRepository,
                       RepairItemRepository repairItemRepository,
                       RepairItemHistoryRepository repairItemHistoryRepository) {
        this.shopRepository = shopRepository;
        this.shopSettingsRepository = shopSettingsRepository;
        this.appUserRepository = appUserRepository;
        this.customerRepository = customerRepository;
        this.repairItemRepository = repairItemRepository;
        this.repairItemHistoryRepository = repairItemHistoryRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Shop> findBySlug(String slug) {
        if (!StringUtils.hasText(slug)) {
            return Optional.empty();
        }
        return shopRepository.findBySlugIgnoreCase(slug.trim());
    }

    @Transactional(readOnly = true)
    public Optional<Shop> findSingleShop() {
        List<Shop> shops = shopRepository.findAllByOrderByCreatedAtAsc();
        return shops.size() == 1 ? Optional.of(shops.get(0)) : Optional.empty();
    }

    @Transactional(readOnly = true)
    public boolean hasAnyShops() {
        return shopRepository.count() > 0;
    }

    @Transactional(readOnly = true)
    public boolean existsBySlug(String slug) {
        return StringUtils.hasText(slug) && shopRepository.existsBySlugIgnoreCase(slug.trim());
    }

    @Transactional
    public Shop getOrCreateDefaultShop() {
        return getOrCreateDefaultShop(DEFAULT_SHOP_NAME);
    }

    @Transactional
    public Shop createShop(String preferredName, String preferredSlug) {
        String resolvedSlug = normalizeShopSlug(preferredSlug);
        if (!isValidShopSlug(resolvedSlug)) {
            throw new IllegalArgumentException("invalid-shop-slug");
        }
        if (shopRepository.existsBySlugIgnoreCase(resolvedSlug)) {
            throw new IllegalArgumentException("duplicate-shop-slug");
        }

        Shop shop = new Shop();
        shop.setName(resolveShopName(preferredName));
        shop.setSlug(resolvedSlug);
        return shopRepository.save(shop);
    }

    @Transactional
    public Shop getOrCreateDefaultShop(String preferredName) {
        return shopRepository.findBySlugIgnoreCase(DEFAULT_SHOP_SLUG)
            .orElseGet(() -> createDefaultShop(preferredName));
    }

    @Transactional
    public Shop syncDefaultShopName(String preferredName) {
        String resolvedName = resolveShopName(preferredName);
        Shop shop = getOrCreateDefaultShop(resolvedName);
        if (!resolvedName.equals(shop.getName())) {
            shop.setName(resolvedName);
            shop = shopRepository.save(shop);
        }
        return shop;
    }

    @Transactional
    public Shop updateShopName(Shop shop, String preferredName) {
        String resolvedName = resolveShopName(preferredName);
        if (!resolvedName.equals(shop.getName())) {
            shop.setName(resolvedName);
            shop = shopRepository.save(shop);
        }
        return shop;
    }

    @Transactional
    public void backfillLegacySingleTenantData() {
        long usersWithoutShop = appUserRepository.countByShopIsNull();
        long customersWithoutShop = customerRepository.countByShopIsNull();
        long repairItemsWithoutShop = repairItemRepository.countByShopIsNull();
        long historyEntriesWithoutShop = repairItemHistoryRepository.countByShopIsNull();
        boolean hasLegacySettings = shopSettingsRepository.findTopByOrderByIdAsc()
            .map(ShopSettings::getBusinessName)
            .filter(StringUtils::hasText)
            .isPresent();

        boolean needsBackfill = usersWithoutShop > 0
            || customersWithoutShop > 0
            || repairItemsWithoutShop > 0
            || historyEntriesWithoutShop > 0
            || (shopRepository.count() == 0 && hasLegacySettings);

        if (!needsBackfill) {
            log.info("Phase 1 tenant backfill skipped: no legacy single-tenant rows require migration.");
            return;
        }

        Shop defaultShop = syncDefaultShopName(resolveLegacyShopName());

        int updatedUsers = appUserRepository.assignShopWhereNull(defaultShop);
        int updatedCustomers = customerRepository.assignShopWhereNull(defaultShop);
        int updatedRepairItems = repairItemRepository.assignShopWhereNull(defaultShop);
        int updatedHistoryEntries = repairItemHistoryRepository.assignShopWhereNull(defaultShop);

        log.info(
            "Phase 1 tenant backfill completed for shop '{}' (slug='{}'): usersUpdated={}, customersUpdated={}, repairItemsUpdated={}, historyEntriesUpdated={}",
            defaultShop.getName(),
            defaultShop.getSlug(),
            updatedUsers,
            updatedCustomers,
            updatedRepairItems,
            updatedHistoryEntries
        );
    }

    private Shop createDefaultShop(String preferredName) {
        Shop shop = new Shop();
        shop.setName(resolveShopName(preferredName));
        shop.setSlug(DEFAULT_SHOP_SLUG);
        Shop saved = shopRepository.save(shop);
        log.info("Created default shop '{}' with slug '{}'.", saved.getName(), saved.getSlug());
        return saved;
    }

    private String resolveLegacyShopName() {
        return shopSettingsRepository.findTopByOrderByIdAsc()
            .map(ShopSettings::getBusinessName)
            .filter(StringUtils::hasText)
            .map(String::trim)
            .orElse(DEFAULT_SHOP_NAME);
    }

    private String resolveShopName(String preferredName) {
        return StringUtils.hasText(preferredName) ? preferredName.trim() : DEFAULT_SHOP_NAME;
    }

    public String normalizeShopSlug(String rawSlug) {
        if (!StringUtils.hasText(rawSlug)) {
            return null;
        }
        String normalized = rawSlug.trim()
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9-]", "-")
            .replaceAll("-{2,}", "-")
            .replaceAll("^-+", "")
            .replaceAll("-+$", "");
        return normalized;
    }

    public boolean isValidShopSlug(String slug) {
        return StringUtils.hasText(slug)
            && slug.length() >= 3
            && slug.length() <= 120
            && slug.matches("^[a-z0-9]+(?:-[a-z0-9]+)*$");
    }
}
