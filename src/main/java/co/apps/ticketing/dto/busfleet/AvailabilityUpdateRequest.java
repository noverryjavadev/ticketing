package co.apps.ticketing.dto.busfleet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AvailabilityUpdateRequest {

    private Long id;

    private boolean available;

    private String newStatus;
}