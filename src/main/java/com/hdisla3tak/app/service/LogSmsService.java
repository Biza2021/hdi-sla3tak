package com.hdisla3tak.app.service;

import com.hdisla3tak.app.config.AppProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
public class LogSmsService implements SmsService {

    private final AppProperties appProperties;

    public LogSmsService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public Optional<SmsComposeAction> prepareComposeAction(String recipientPhoneNumber,
                                                           SmsMessageType messageType,
                                                           String messageText,
                                                           String trackingUrl) {
        if (!appProperties.getSms().isEnabled()) {
            return Optional.empty();
        }
        String normalizedPhoneNumber = normalizePhoneNumber(recipientPhoneNumber);
        if (!StringUtils.hasText(normalizedPhoneNumber) || !StringUtils.hasText(messageText)) {
            return Optional.empty();
        }
        String encodedBody = UriUtils.encodeQueryParam(messageText, StandardCharsets.UTF_8);
        String smsUrl = "sms:" + normalizedPhoneNumber + "?body=" + encodedBody;
        return Optional.of(new SmsComposeAction(
            messageType,
            normalizedPhoneNumber,
            messageText,
            trackingUrl,
            smsUrl
        ));
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber)) {
            return null;
        }
        String normalized = phoneNumber.trim().replaceAll("[\\s()\\-]", "");
        return StringUtils.hasText(normalized) ? normalized : null;
    }
}
