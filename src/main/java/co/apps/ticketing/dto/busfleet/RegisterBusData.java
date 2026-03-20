package co.apps.ticketing.dto.busfleet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterBusData {

    private String regNumber;
    private String busBrand;
    private String busType;
    private Long numOfSeat;
    private boolean available;
    private String status;
    private String desc;
}
