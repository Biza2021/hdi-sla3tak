package com.hdisla3tak.app.service;

public class SmsComposeAction {

    private final SmsMessageType messageType;
    private final String recipientPhoneNumber;
    private final String messageText;
    private final String trackingUrl;
    private final String smsUrl;

    public SmsComposeAction(SmsMessageType messageType,
                            String recipientPhoneNumber,
                            String messageText,
                            String trackingUrl,
                            String smsUrl) {
        this.messageType = messageType;
        this.recipientPhoneNumber = recipientPhoneNumber;
        this.messageText = messageText;
        this.trackingUrl = trackingUrl;
        this.smsUrl = smsUrl;
    }

    public SmsMessageType getMessageType() {
        return messageType;
    }

    public String getRecipientPhoneNumber() {
        return recipientPhoneNumber;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getTrackingUrl() {
        return trackingUrl;
    }

    public String getSmsUrl() {
        return smsUrl;
    }
}
