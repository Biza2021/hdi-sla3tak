package com.hdisla3tak.app.service;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class SmsMessageComposer {

    private final MessageSource messageSource;
    private final ShopSettingsService shopSettingsService;

    public SmsMessageComposer(MessageSource messageSource,
                              ShopSettingsService shopSettingsService) {
        this.messageSource = messageSource;
        this.shopSettingsService = shopSettingsService;
    }

    public String compose(SmsMessageType messageType, String pickupCode, String trackingUrl) {
        String key = switch (messageType) {
            case ITEM_REGISTERED -> "sms.itemRegistered";
            case READY_FOR_PICKUP -> "sms.readyForPickup";
        };
        Locale locale = LocaleContextHolder.getLocale();
        String shopName = shopSettingsService.getBusinessName();
        Object[] args = {shopName, pickupCode, trackingUrl};
        return messageSource.getMessage(key, args, locale);
    }
}
