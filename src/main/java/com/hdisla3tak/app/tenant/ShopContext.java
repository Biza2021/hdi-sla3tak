package com.hdisla3tak.app.tenant;

import com.hdisla3tak.app.domain.Shop;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ShopContext {

    private final ThreadLocal<Shop> currentShop = new ThreadLocal<>();

    public void setCurrentShop(Shop shop) {
        currentShop.set(shop);
    }

    public Optional<Shop> getCurrentShop() {
        return Optional.ofNullable(currentShop.get());
    }

    public Shop requireCurrentShop() {
        return getCurrentShop().orElseThrow(() -> new IllegalStateException("No active shop context."));
    }

    public Optional<String> getCurrentShopSlug() {
        return getCurrentShop().map(Shop::getSlug);
    }

    public void clear() {
        currentShop.remove();
    }
}
