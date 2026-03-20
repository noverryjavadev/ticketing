package co.apps.ticketing.dto.busfleet;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BusFleetList {

    private Long id;
    private String regNumber;
    private String busBrand;
    private String busType;
    private Long numberOfSeat;
    private boolean availability;
    private String fleetStatus;
}
