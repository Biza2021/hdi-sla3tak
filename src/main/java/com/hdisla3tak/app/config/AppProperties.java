package com.hdisla3tak.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Storage storage = new Storage();
    private final Notifications notifications = new Notifications();
    private final Sms sms = new Sms();

    public Storage getStorage() {
        return storage;
    }

    public Notifications getNotifications() {
        return notifications;
    }

    public Sms getSms() {
        return sms;
    }

    public static class Storage {
        private String uploadDir = "uploads/items";

        public String getUploadDir() {
            return uploadDir;
        }

        public void setUploadDir(String uploadDir) {
            this.uploadDir = uploadDir;
        }
    }

    public static class Notifications {
        private boolean enabled = false;
        private String provider = "log";
        private String whatsappFrom = "whatsapp:+14155238886";
        private String twilioAccountSid;
        private String twilioAuthToken;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getWhatsappFrom() {
            return whatsappFrom;
        }

        public void setWhatsappFrom(String whatsappFrom) {
            this.whatsappFrom = whatsappFrom;
        }

        public String getTwilioAccountSid() {
            return twilioAccountSid;
        }

        public void setTwilioAccountSid(String twilioAccountSid) {
            this.twilioAccountSid = twilioAccountSid;
        }

        public String getTwilioAuthToken() {
            return twilioAuthToken;
        }

        public void setTwilioAuthToken(String twilioAuthToken) {
            this.twilioAuthToken = twilioAuthToken;
        }
    }

    public static class Sms {
        private boolean enabled = true;
        private String mode = "device-link";
        private String baseUrl = "";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }
}
