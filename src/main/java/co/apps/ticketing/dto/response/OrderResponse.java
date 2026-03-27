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
public class OrderResponse {
    private Long orderId;
    private String orderNumber;
    private String status;
    private Double totalPrice;
    private LocalDateTime expireAt;
    private String paymentUrl; // URL untuk pembayaran
    private String message;
}
