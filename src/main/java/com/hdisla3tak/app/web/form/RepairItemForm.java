package com.hdisla3tak.app.web.form;

import com.hdisla3tak.app.domain.enums.ItemCategory;
import com.hdisla3tak.app.domain.enums.RepairStatus;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RepairItemForm {

    @NotNull(message = "{validation.item.customer.required}")
    private Long customerId;

    @NotNull(message = "{validation.item.category.required}")
    private ItemCategory category;

    @NotBlank(message = "{validation.item.title.required}")
    @Size(max = 140, message = "{validation.item.title.size}")
    private String title;

    @Size(max = 1500, message = "{validation.item.description.size}")
    private String description;

    @NotNull(message = "{validation.item.dateReceived.required}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateReceived;

    @Size(max = 1500, message = "{validation.item.repairNotes.size}")
    private String repairNotes;

    @NotNull(message = "{validation.item.status.required}")
    private RepairStatus status;

    @DecimalMin(value = "0.0", inclusive = true, message = "{validation.money.nonNegative}")
    private BigDecimal estimatedPrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "{validation.money.nonNegative}")
    private BigDecimal depositPaid;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate expectedDeliveryDate;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public ItemCategory getCategory() { return category; }
    public void setCategory(ItemCategory category) { this.category = category; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getDateReceived() { return dateReceived; }
    public void setDateReceived(LocalDate dateReceived) { this.dateReceived = dateReceived; }
    public String getRepairNotes() { return repairNotes; }
    public void setRepairNotes(String repairNotes) { this.repairNotes = repairNotes; }
    public RepairStatus getStatus() { return status; }
    public void setStatus(RepairStatus status) { this.status = status; }
    public BigDecimal getEstimatedPrice() { return estimatedPrice; }
    public void setEstimatedPrice(BigDecimal estimatedPrice) { this.estimatedPrice = estimatedPrice; }
    public BigDecimal getDepositPaid() { return depositPaid; }
    public void setDepositPaid(BigDecimal depositPaid) { this.depositPaid = depositPaid; }
    public LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) { this.expectedDeliveryDate = expectedDeliveryDate; }
}
