package co.apps.ticketing.filter;

import co.apps.ticketing.entity.logging.RequestLog;
import co.apps.ticketing.repository.logging.RequestLogRepository;
import co.apps.ticketing.service.masking.MaskingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Order(1)
public class LoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    private static final String MASKING_VERSION = "1.0";

    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/actuator", "/health", "/metrics", "/static", "/css", "/js"
    );

    @Autowired
    private RequestLogRepository requestLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MaskingService maskingService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Skip logging untuk excluded paths
        if (shouldNotFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();

        CachedBodyHttpServletRequest cachedBodyRequest = new CachedBodyHttpServletRequest(request);
        CachedBodyHttpServletResponse cachedBodyResponse = new CachedBodyHttpServletResponse(response);

        try {
            filterChain.doFilter(cachedBodyRequest, cachedBodyResponse);

            long executionTime = System.currentTimeMillis() - startTime;

            // Ambil original body
            String originalRequestBody = cachedBodyRequest.getRequestBody();
            String originalResponseBody = cachedBodyResponse.getResponseBody();

            // Mask sensitive data
            String maskedRequestBody = maskingService.maskRequestBody(originalRequestBody);
            String maskedResponseBody = maskingService.maskResponseBody(originalResponseBody);
            Map<String, String> maskedHeaders = maskingService.maskHeaders(getHeaders(request));

            // Create log entry with masked data
            RequestLog requestLog = new RequestLog();
            requestLog.setRequestId(requestId);
            requestLog.setMethod(request.getMethod());
            requestLog.setUrl(request.getRequestURI());
            requestLog.setHeaders(maskedHeaders);
            requestLog.setRequestBody(maskedRequestBody);
            requestLog.setResponseBody(maskedResponseBody);
            requestLog.setStatusCode(response.getStatus());
            requestLog.setTimestamp(LocalDateTime.now());
            requestLog.setExecutionTime(executionTime);
            requestLog.setClientIp(getClientIp(request));
            requestLog.setUserAgent(request.getHeader("User-Agent"));
            requestLog.setMasked(true);
            requestLog.setMaskingVersion(MASKING_VERSION);

            // Optional: Simpan juga original data di collection terpisah (audit)
            // Ini hanya jika diperlukan untuk debugging dan dengan akses terbatas
            if (isSensitiveRequest(request)) {
                saveOriginalDataToAuditCollection(requestId, originalRequestBody, originalResponseBody);
            }

            // Save to MongoDB
            saveLogAsync(requestLog);

            logger.info("Request processed: {} {} - Status: {} - Time: {}ms (masked: {})",
                    request.getMethod(), request.getRequestURI(), response.getStatus(),
                    executionTime, requestLog.isMasked());

            // Copy response
            byte[] responseData = cachedBodyResponse.getResponseData();
            response.getOutputStream().write(responseData);

        } catch (Exception e) {
            logger.error("Error processing request", e);
            throw e;
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean isSensitiveRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.contains("/login") ||
                path.contains("/register") ||
                path.contains("/change-password") ||
                path.contains("/reset-password");
    }

    private void saveOriginalDataToAuditCollection(String requestId,
                                                   String originalRequestBody,
                                                   String originalResponseBody) {
        // Simpan ke collection terpisah dengan akses terbatas
        // Ini opsional, hanya untuk debugging dengan izin khusus
        new Thread(() -> {
            try {
                // Implementasi saving ke collection audit
                // Misalnya menggunakan repository khusus dengan annotation @Secure
                logger.debug("Original data saved for audit: {}", requestId);
            } catch (Exception e) {
                logger.error("Failed to save audit data", e);
            }
        }).start();
    }

    private Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                headers.put(headerName, request.getHeader(headerName));
            }
        }
        return headers;
    }

    private String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    private void saveLogAsync(RequestLog requestLog) {
        new Thread(() -> {
            try {
                requestLogRepository.save(requestLog);
                logger.debug("Log saved successfully for request: {}", requestLog.getRequestId());
            } catch (Exception e) {
                logger.error("Failed to save log to MongoDB", e);
            }
        }).start();
    }
}
