package com.hdisla3tak.app.service;

import com.hdisla3tak.app.config.AppProperties;
import com.hdisla3tak.app.domain.RepairItem;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
public class NotificationService {

    private final AppProperties appProperties;
    private final SmsService smsService;
    private final SmsMessageComposer smsMessageComposer;

    public NotificationService(AppProperties appProperties,
                               SmsService smsService,
                               SmsMessageComposer smsMessageComposer) {
        this.appProperties = appProperties;
        this.smsService = smsService;
        this.smsMessageComposer = smsMessageComposer;
    }

    public Optional<SmsComposeAction> prepareItemRegistered(RepairItem item, String requestBaseUrl) {
        return prepareComposeAction(item, SmsMessageType.ITEM_REGISTERED, requestBaseUrl);
    }

    public Optional<SmsComposeAction> prepareReadyForPickup(RepairItem item, String requestBaseUrl) {
        return prepareComposeAction(item, SmsMessageType.READY_FOR_PICKUP, requestBaseUrl);
    }

    private Optional<SmsComposeAction> prepareComposeAction(RepairItem item,
                                                            SmsMessageType messageType,
                                                            String requestBaseUrl) {
        String trackingUrl = buildTrackingUrl(item, requestBaseUrl);
        String messageText = smsMessageComposer.compose(messageType, item.getPickupCode(), trackingUrl);
        return smsService.prepareComposeAction(item.getCustomer().getPhoneNumber(), messageType, messageText, trackingUrl);
    }

    private String buildTrackingUrl(RepairItem item, String requestBaseUrl) {
        String path = "/track/" + item.getPublicTrackingToken();
        String baseUrl = StringUtils.hasText(appProperties.getSms().getBaseUrl())
            ? appProperties.getSms().getBaseUrl()
            : requestBaseUrl;
        if (!StringUtils.hasText(baseUrl)) {
            return path;
        }

        String normalizedBaseUrl = baseUrl.trim();
        if (normalizedBaseUrl.endsWith("/")) {
            normalizedBaseUrl = normalizedBaseUrl.substring(0, normalizedBaseUrl.length() - 1);
        }
        return normalizedBaseUrl + path;
    }
}
