package co.apps.ticketing.enums;

public enum SeatStatus {
    AVAILABLE("Tersedia"),
    LOCKED("Dikunci"),
    BOOKED("Terpesan");

    private final String description;

    SeatStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
