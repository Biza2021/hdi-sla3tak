package com.hdisla3tak.app.domain.enums;

public enum ItemCategory {
    WATCH("Watch"),
    PHONE("Phone"),
    FURNITURE("Furniture"),
    APPLIANCE("Appliance"),
    LAPTOP("Laptop"),
    TABLET("Tablet"),
    JEWELRY("Jewelry"),
    OTHER("Other");

    private final String label;

    ItemCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
