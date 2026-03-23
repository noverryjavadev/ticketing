package co.apps.ticketing.entity.logging;

import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Document(collection = "request_logs")
public class RequestLog {

    @Id
    private String id;
    private String requestId;
    private String method;
    private String url;
    private Map<String, String> headers;
    private String requestBody;
    private String responseBody;
    private int statusCode;
    private LocalDateTime timestamp;
    private long executionTime;
    private String clientIp;
    private String userAgent;
    private boolean isMasked;  // Flag untuk menandai data sudah di-mask
    private String maskingVersion; // Versi masking untuk tracking

    public RequestLog() {}
}
