package co.apps.ticketing.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "seats")
public class Seat {
    @Id
    private Long id;
    private Long scheduleId;
    private String seatNumber; // "A1", "A2", etc.
    private String status; // AVAILABLE, LOCKED, BOOKED
    private String lockedBy; // userId yang mengunci
    private LocalDateTime lockedAt;
    private Integer version;
    private Long price;
}
