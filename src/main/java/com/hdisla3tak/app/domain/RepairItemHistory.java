package com.hdisla3tak.app.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "repair_item_history")
public class RepairItemHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repair_item_id")
    private RepairItem repairItem;

    @Column(nullable = false, length = 80)
    private String actionType;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(length = 120)
    private String actorName;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public RepairItem getRepairItem() { return repairItem; }
    public void setRepairItem(RepairItem repairItem) { this.repairItem = repairItem; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
