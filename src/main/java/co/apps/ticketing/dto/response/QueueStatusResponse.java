package co.apps.ticketing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueStatusResponse {
    private Long position;
    private Long estimatedWaitTimeSeconds;
    private String status; // WAITING, PROCESSING, REDIRECTING
    private String token; // Token untuk akses ke halaman booking
}
