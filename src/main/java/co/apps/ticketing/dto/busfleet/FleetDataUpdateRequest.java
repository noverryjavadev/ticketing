package co.apps.ticketing.dto.busfleet;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
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
