package com.hdisla3tak.app.service;

import java.util.Optional;

public interface SmsService {

    Optional<SmsComposeAction> prepareComposeAction(String recipientPhoneNumber,
                                                    SmsMessageType messageType,
                                                    String messageText,
                                                    String trackingUrl);
}
