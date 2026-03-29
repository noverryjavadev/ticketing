package co.apps.ticketing.dto.response;

import co.apps.ticketing.enums.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SeatMapResponse {
    private Long scheduleId;
    private String busName;
    private String from;
    private String to;
    private LocalDateTime departureTime;
    private Double basePrice;

    // Struktur peta kursi (misal: 2-2 format untuk bus)
    private SeatLayout seatLayout;

    // Ringkasan ketersediaan
    private SeatSummary summary;
}

