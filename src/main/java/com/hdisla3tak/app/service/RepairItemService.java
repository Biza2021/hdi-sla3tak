package com.hdisla3tak.app.service;

import com.hdisla3tak.app.config.AppProperties;
import com.hdisla3tak.app.domain.Customer;
import com.hdisla3tak.app.domain.RepairItem;
import com.hdisla3tak.app.domain.RepairItemHistory;
import com.hdisla3tak.app.domain.enums.RepairStatus;
import com.hdisla3tak.app.repository.CustomerRepository;
import com.hdisla3tak.app.repository.RepairItemHistoryRepository;
import com.hdisla3tak.app.repository.RepairItemRepository;
import com.hdisla3tak.app.web.form.DeliveryForm;
import com.hdisla3tak.app.web.form.RepairItemForm;
import com.hdisla3tak.app.web.spec.RepairItemSpecifications;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class RepairItemService {

    private static final int TRACKING_TOKEN_BYTES = 24;

    private final RepairItemRepository repairItemRepository;
    private final RepairItemHistoryRepository historyRepository;
    private final CustomerRepository customerRepository;
    private final FileStorageService fileStorageService;
    private final AppProperties appProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public RepairItemService(RepairItemRepository repairItemRepository,
                             RepairItemHistoryRepository historyRepository,
                             CustomerRepository customerRepository,
                             FileStorageService fileStorageService,
                             AppProperties appProperties) {
        this.repairItemRepository = repairItemRepository;
        this.historyRepository = historyRepository;
        this.customerRepository = customerRepository;
        this.fileStorageService = fileStorageService;
        this.appProperties = appProperties;
    }

    public List<RepairItem> findAll(String q, RepairStatus status, String delivered, String category) {
        Specification<RepairItem> specification = Specification.allOf(
            RepairItemSpecifications.matchesSearch(q),
            RepairItemSpecifications.hasStatus(status),
            RepairItemSpecifications.hasDelivered(delivered),
            RepairItemSpecifications.hasCategory(category)
        );
        return repairItemRepository.findAll(specification, Sort.by(Sort.Direction.DESC, "updatedAt"));
    }

    public RepairItem getById(Long id) {
        RepairItem item = repairItemRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Repair item not found."));
        return ensurePublicTrackingToken(item);
    }

    public Optional<RepairItem> findByPublicTrackingToken(String token) {
        if (!StringUtils.hasText(token)) {
            return Optional.empty();
        }
        return repairItemRepository.findByPublicTrackingToken(token.trim());
    }

    public String getPublicTrackingPath(RepairItem item) {
        return "/track/" + ensurePublicTrackingToken(item).getPublicTrackingToken();
    }

    public String buildPublicTrackingUrl(String baseUrl, RepairItem item) {
        String normalizedBaseUrl = baseUrl == null ? "" : baseUrl.trim();
        if (normalizedBaseUrl.endsWith("/")) {
            normalizedBaseUrl = normalizedBaseUrl.substring(0, normalizedBaseUrl.length() - 1);
        }
        return normalizedBaseUrl + getPublicTrackingPath(item);
    }

    public RepairItem create(RepairItemForm form, MultipartFile image, String actor) {
        RepairItem item = new RepairItem();
        apply(item, form, image, true);
        RepairItem saved = repairItemRepository.save(item);
        log(saved, "ITEM_CREATED", "Repair item was registered.", actor);
        log(saved, "SMS_NOTIFICATION", historyMessage(SmsMessageType.ITEM_REGISTERED, saved), actor);
        return saved;
    }

    public RepairItem update(Long id, RepairItemForm form, MultipartFile image, String actor) {
        RepairItem item = getById(id);
        RepairStatus previousStatus = item.getStatus();
        String previousNotes = item.getRepairNotes();
        apply(item, form, image, false);
        RepairItem saved = repairItemRepository.save(item);
        if (previousStatus != saved.getStatus()) {
            log(saved, "STATUS_CHANGED", "Status changed from %s to %s.".formatted(previousStatus.name(), saved.getStatus().name()), actor);
            if (saved.getStatus() == RepairStatus.READY_FOR_PICKUP && previousStatus != RepairStatus.READY_FOR_PICKUP) {
                log(saved, "SMS_NOTIFICATION", historyMessage(SmsMessageType.READY_FOR_PICKUP, saved), actor);
            }
        }
        if (!safe(previousNotes).equals(safe(saved.getRepairNotes()))) {
            log(saved, "NOTES_UPDATED", "Repair notes were updated.", actor);
        }
        return saved;
    }

    public RepairItem deliver(Long id, DeliveryForm form, String actor) {
        RepairItem item = getById(id);
        if (item.isDelivered()) {
            throw new IllegalStateException("already-delivered");
        }
        item.setDeliveredAt(LocalDateTime.now());
        item.setDeliveredBy(StringUtils.hasText(form.getDeliveredBy()) ? form.getDeliveredBy().trim() : actor);
        item.setDeliveryConfirmationNote(trimToNull(form.getDeliveryConfirmationNote()));
        item.setStatus(RepairStatus.DELIVERED);
        RepairItem saved = repairItemRepository.save(item);
        log(saved, "DELIVERED", "Item was delivered to customer.", actor);
        return saved;
    }

    public List<RepairItem> searchForDelivery(String q) {
        if (!StringUtils.hasText(q)) {
            return List.of();
        }
        return repairItemRepository.searchForDelivery(q.trim());
    }

    private void apply(RepairItem item, RepairItemForm form, MultipartFile image, boolean creating) {
        Customer customer = customerRepository.findById(form.getCustomerId())
            .orElseThrow(() -> new IllegalArgumentException("Selected customer does not exist."));
        item.setCustomer(customer);
        item.setCategory(form.getCategory());
        item.setTitle(form.getTitle().trim());
        item.setDescription(trimToNull(form.getDescription()));
        item.setDateReceived(form.getDateReceived());
        item.setRepairNotes(trimToNull(form.getRepairNotes()));
        item.setStatus(form.getStatus());
        item.setEstimatedPrice(form.getEstimatedPrice());
        item.setDepositPaid(form.getDepositPaid());
        item.setExpectedDeliveryDate(form.getExpectedDeliveryDate());
        item.setRemainingBalance(calculateRemaining(form.getEstimatedPrice(), form.getDepositPaid()));

        if (creating) {
            item.setPickupCode(generatePickupCode());
            item.setPublicTrackingToken(generatePublicTrackingToken());
        }

        if (image != null && !image.isEmpty()) {
            item.setImagePath(fileStorageService.storeImage(image));
        }
    }

    private BigDecimal calculateRemaining(BigDecimal estimatedPrice, BigDecimal depositPaid) {
        if (estimatedPrice == null) {
            return null;
        }
        BigDecimal deposit = depositPaid == null ? BigDecimal.ZERO : depositPaid;
        BigDecimal remaining = estimatedPrice.subtract(deposit);
        return remaining.signum() < 0 ? BigDecimal.ZERO : remaining;
    }

    private String generatePickupCode() {
        String code;
        do {
            code = "HS-" + (100000 + secureRandom.nextInt(900000));
        } while (repairItemRepository.existsByPickupCode(code));
        return code;
    }

    private RepairItem ensurePublicTrackingToken(RepairItem item) {
        if (StringUtils.hasText(item.getPublicTrackingToken())) {
            return item;
        }
        item.setPublicTrackingToken(generatePublicTrackingToken());
        return repairItemRepository.save(item);
    }

    private String generatePublicTrackingToken() {
        String token;
        do {
            byte[] tokenBytes = new byte[TRACKING_TOKEN_BYTES];
            secureRandom.nextBytes(tokenBytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        } while (repairItemRepository.existsByPublicTrackingToken(token));
        return token;
    }

    private void log(RepairItem item, String action, String message, String actor) {
        RepairItemHistory history = new RepairItemHistory();
        history.setRepairItem(item);
        history.setActionType(action);
        history.setMessage(message);
        history.setActorName(actor);
        historyRepository.save(history);
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String historyMessage(SmsMessageType messageType, RepairItem item) {
        String smsLabel = switch (messageType) {
            case ITEM_REGISTERED -> "Registration SMS";
            case READY_FOR_PICKUP -> "Ready-for-pickup SMS";
        };

        if (!appProperties.getSms().isEnabled()) {
            return smsLabel + " compose action was not prepared because SMS compose support is disabled.";
        }
        return StringUtils.hasText(item.getCustomer().getPhoneNumber())
            ? smsLabel + " compose action was prepared."
            : smsLabel + " compose action was not prepared because the customer phone number is missing.";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
