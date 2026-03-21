package co.apps.ticketing.dto.busfleet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FleetDataUpdateRequest {

    private Long id;
    private String regNumber;
    private String busType;
    private Long numOfSeat;
    private boolean available;
    private String status;
    private String desc;
    private String maintenanceSchedule;

}
