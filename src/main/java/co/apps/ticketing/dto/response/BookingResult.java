package co.apps.ticketing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResult {

    private Boolean success;
    private String message;
    private OrderResponse orderResponse;
    private String errorCode;
}
