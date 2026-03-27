package co.apps.ticketing.dto.orders;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {

    @NotNull(message = "Schedule ID tidak boleh kosong")
    private Long scheduleId;

    @NotEmpty(message = "Minimal pilih 1 kursi")
    private List<String> seatNumbers;

    @NotNull(message = "User ID tidak boleh kosong")
    private Long userId;

    private String idempotencyKey;
}
