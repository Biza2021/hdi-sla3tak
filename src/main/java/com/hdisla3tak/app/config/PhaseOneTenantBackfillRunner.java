package com.hdisla3tak.app.config;

import com.hdisla3tak.app.service.ShopService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class PhaseOneTenantBackfillRunner implements ApplicationRunner {

    private final ShopService shopService;

    public PhaseOneTenantBackfillRunner(ShopService shopService) {
        this.shopService = shopService;
    }

    @Override
    public void run(ApplicationArguments args) {
        shopService.backfillLegacySingleTenantData();
    }
}
