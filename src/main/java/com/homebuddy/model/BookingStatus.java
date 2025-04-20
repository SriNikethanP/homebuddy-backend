package com.homebuddy.model;

public enum BookingStatus {
    PENDING("Pending"),
    NOT_CALLED("Not Called"),
    CALLED_PENDING("Called - Pending"),
    CALL_AGAIN("Call Again"),
    CONFIRMED("Confirmed"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String displayName;

    BookingStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 