package com.hdisla3tak.app.domain.enums;

public enum RepairStatus {
    RECEIVED("Received"),
    UNDER_DIAGNOSIS("Under Diagnosis"),
    WAITING_FOR_PARTS("Waiting for Parts"),
    UNDER_REPAIR("Under Repair"),
    FIXED("Fixed"),
    READY_FOR_PICKUP("Ready for Pickup"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    private final String label;

    RepairStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
