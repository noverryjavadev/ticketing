package co.apps.ticketing.enums;

public enum OrderStatus {
    PENDING("Menunggu Pembayaran"),
    PAID("Sudah Dibayar"),
    CANCELLED("Dibatalkan"),
    EXPIRED("Kadaluarsa");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
