package co.apps.ticketing.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class PricingInfo {
    private Double basePrice;
    private Double tax;
    private Double totalPrice;
    private Map<String, Double> seatTypePrices; // "VIP" -> 200000
}
