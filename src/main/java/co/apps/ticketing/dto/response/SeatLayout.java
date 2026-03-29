package co.apps.ticketing.dto.response;

import co.apps.ticketing.enums.SeatStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class SeatLayout {
    private List<String> seatRows;      // ["A1","A2",...]
    private Map<String, String> seatStatusMap;  // "A1" -> "AVAILABLE"
    private Map<String, Double> seatPriceMap;       // "A1" -> 150000 (jika harga beda per kursi)
}
