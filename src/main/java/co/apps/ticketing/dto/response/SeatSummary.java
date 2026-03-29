package co.apps.ticketing.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeatSummary {
    private Integer totalSeats;
    private Integer availableSeats;
    private Integer lockedSeats;    // Sedang diproses user lain
    private Integer bookedSeats;
}
