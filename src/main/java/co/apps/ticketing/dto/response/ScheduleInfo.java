package co.apps.ticketing.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ScheduleInfo {
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String duration;
    private List<String> stops;           // Berhenti di kota apa saja
    private List<Integer> stopDurations;  // Durasi berhenti
}
