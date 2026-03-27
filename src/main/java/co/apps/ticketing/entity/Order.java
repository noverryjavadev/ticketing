package co.apps.ticketing.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "_orders")
@Entity
public class Order {

    @Id
    private Long id;
    private String orderNumber;
    private Long userId;
    private Long scheduleId;
    private String seatNumbers; // "A1,A2,A3" format
    private Integer totalSeats;
    private Double totalPrice;
    private String status; // PENDING, PAID, CANCELLED, EXPIRED
    private LocalDateTime expireAt; // Batas waktu pembayaran
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Optimistic Locking version
    private Integer version;
}
