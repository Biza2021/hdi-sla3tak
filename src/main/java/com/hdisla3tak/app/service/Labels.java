package com.hdisla3tak.app.service;

import com.hdisla3tak.app.domain.RepairItemHistory;
import com.hdisla3tak.app.domain.enums.ItemCategory;
import com.hdisla3tak.app.domain.enums.RepairStatus;
import com.hdisla3tak.app.domain.enums.UserRole;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("labels")
public class Labels {

    private static final Pattern STATUS_CHANGED_PATTERN = Pattern.compile("Status changed from ([A-Z_]+) to ([A-Z_]+)\\.");

    private final MessageSource messageSource;

    public Labels(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String msg(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    public String msg(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    public String status(RepairStatus status) {
        return msg("status." + status.name().toLowerCase());
    }

    public String category(ItemCategory category) {
        return msg("category." + category.name().toLowerCase());
    }

    public String role(UserRole role) {
        return msg("role." + role.name().toLowerCase());
    }

    public String historyAction(String actionType) {
        if (actionType == null) {
            return "";
        }
        return switch (actionType) {
            case "ITEM_CREATED" -> msg("history.action.item_created");
            case "STATUS_CHANGED" -> msg("history.action.status_changed");
            case "NOTES_UPDATED" -> msg("history.action.notes_updated");
            case "DELIVERED" -> msg("history.action.delivered");
            case "SMS_NOTIFICATION" -> msg("history.action.sms_notification");
            default -> actionType;
        };
    }

    public String historyMessage(RepairItemHistory entry) {
        if (entry == null) {
            return "";
        }

        String actionType = entry.getActionType();
        String message = entry.getMessage();
        if (actionType == null) {
            return message == null ? "" : message;
        }

        return switch (actionType) {
            case "ITEM_CREATED" -> msg("history.message.item_created");
            case "NOTES_UPDATED" -> msg("history.message.notes_updated");
            case "DELIVERED" -> msg("history.message.delivered");
            case "STATUS_CHANGED" -> localizeStatusChanged(message);
            case "SMS_NOTIFICATION" -> localizeSmsNotification(message);
            default -> message == null ? "" : message;
        };
    }

    private String localizeStatusChanged(String message) {
        Matcher matcher = STATUS_CHANGED_PATTERN.matcher(message == null ? "" : message);
        if (!matcher.matches()) {
            return message == null ? "" : message;
        }
        String fromStatus = matcher.group(1);
        String toStatus = matcher.group(2);
        return msg("history.message.status_changed", statusLabel(fromStatus), statusLabel(toStatus));
    }

    private String localizeSmsNotification(String message) {
        String raw = message == null ? "" : message;
        if (raw.contains("customer phone number is missing")) {
            return msg("history.message.sms_missing_phone");
        }
        if (raw.contains("SMS compose support is disabled") || raw.contains("SMS notifications are disabled")) {
            return msg("history.message.sms_disabled");
        }
        if (raw.contains("Ready-for-pickup SMS")) {
            return msg("history.message.sms_ready_for_pickup");
        }
        if (raw.contains("Registration SMS")) {
            return msg("history.message.sms_item_registered");
        }
        return msg("history.message.sms_prepared");
    }

    private String statusLabel(String statusName) {
        if (statusName == null) {
            return "";
        }
        String key = "status." + statusName.toLowerCase(Locale.ROOT);
        return msg(key);
    }
}
