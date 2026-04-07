package com.hdisla3tak.app.domain;

import com.hdisla3tak.app.domain.enums.ItemCategory;
import com.hdisla3tak.app.domain.enums.RepairStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "repair_items",
    indexes = {
        @Index(name = "idx_repair_items_public_tracking_token", columnList = "public_tracking_token")
    }
)
public class RepairItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ItemCategory category;

    @Column(nullable = false, length = 140)
    private String title;

    @Column(length = 1500)
    private String description;

    @Column(length = 255)
    private String imagePath;

    @Column(nullable = false)
    private LocalDate dateReceived;

    @Column(length = 1500)
    private String repairNotes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RepairStatus status = RepairStatus.RECEIVED;

    @Column(nullable = false, unique = true, length = 20)
    private String pickupCode;

    @Column(name = "public_tracking_token", unique = true, length = 64)
    private String publicTrackingToken;

    @Column(precision = 12, scale = 2)
    private BigDecimal estimatedPrice;

    @Column(precision = 12, scale = 2)
    private BigDecimal depositPaid;

    @Column(precision = 12, scale = 2)
    private BigDecimal remainingBalance;

    private LocalDate expectedDeliveryDate;

    private LocalDateTime deliveredAt;

    @Column(length = 120)
    private String deliveredBy;

    @Column(length = 800)
    private String deliveryConfirmationNote;

    @OneToMany(mappedBy = "repairItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<RepairItemHistory> historyEntries = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isDelivered() {
        return deliveredAt != null || status == RepairStatus.DELIVERED;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public ItemCategory getCategory() { return category; }
    public void setCategory(ItemCategory category) { this.category = category; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public LocalDate getDateReceived() { return dateReceived; }
    public void setDateReceived(LocalDate dateReceived) { this.dateReceived = dateReceived; }
    public String getRepairNotes() { return repairNotes; }
    public void setRepairNotes(String repairNotes) { this.repairNotes = repairNotes; }
    public RepairStatus getStatus() { return status; }
    public void setStatus(RepairStatus status) { this.status = status; }
    public String getPickupCode() { return pickupCode; }
    public void setPickupCode(String pickupCode) { this.pickupCode = pickupCode; }
    public String getPublicTrackingToken() { return publicTrackingToken; }
    public void setPublicTrackingToken(String publicTrackingToken) { this.publicTrackingToken = publicTrackingToken; }
    public BigDecimal getEstimatedPrice() { return estimatedPrice; }
    public void setEstimatedPrice(BigDecimal estimatedPrice) { this.estimatedPrice = estimatedPrice; }
    public BigDecimal getDepositPaid() { return depositPaid; }
    public void setDepositPaid(BigDecimal depositPaid) { this.depositPaid = depositPaid; }
    public BigDecimal getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(BigDecimal remainingBalance) { this.remainingBalance = remainingBalance; }
    public LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) { this.expectedDeliveryDate = expectedDeliveryDate; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
    public String getDeliveredBy() { return deliveredBy; }
    public void setDeliveredBy(String deliveredBy) { this.deliveredBy = deliveredBy; }
    public String getDeliveryConfirmationNote() { return deliveryConfirmationNote; }
    public void setDeliveryConfirmationNote(String deliveryConfirmationNote) { this.deliveryConfirmationNote = deliveryConfirmationNote; }
    public List<RepairItemHistory> getHistoryEntries() { return historyEntries; }
    public void setHistoryEntries(List<RepairItemHistory> historyEntries) { this.historyEntries = historyEntries; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
