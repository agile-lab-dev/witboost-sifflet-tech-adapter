package com.witboost.provisioning.dq.sifflet.model.client;

public enum Criticality {
    CRITICAL(0, "Critical"),
    HIGH(1, "High"),
    MODERATE(2, "Moderate"),
    LOW(3, "Low");

    private final int id;
    private final String name;

    Criticality(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static String getNameById(int id) {
        for (Criticality c : values()) {
            if (c.id == id) {
                return c.name.toUpperCase();
            }
        }
        throw new IllegalArgumentException("Invalid ID: " + id);
    }
}
