package co.apps.ticketing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleSearchResponse {

    private Long scheduleId;
    private String busName;
    private String busType;          // AC, NON_AC, SLEEPER
    private String from;
    private String to;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Double price;
    private Integer availableSeats;   // Ketersediaan tiket
    private Integer totalSeats;
    private String duration;          // Durasi perjalanan (HH:MM)

    // Additional info
    private Boolean isPromo;
    private Double originalPrice;
}
