package co.apps.ticketing.service.masking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MaskingService {

    private static final Set<String> SENSITIVE_FIELDS = new HashSet<>(Arrays.asList(
            "password", "pwd", "pass", "pin",
            "token", "accessToken", "refreshToken",
            "authorization", "secret", "apiKey", "apiSecret",
            "creditCard", "cvv", "cardNumber"
    ));

    private static final String MASKED_VALUE = "******";

    private final ObjectMapper objectMapper = new JsonMapper();

    /**
     * Mask sensitive data in request body
     */
    public String maskRequestBody(String requestBody) {
        if (!StringUtils.hasText(requestBody)) {
            return requestBody;
        }

        try {
            // Parse JSON body
            JsonNode jsonNode = objectMapper.readTree(requestBody);
            maskSensitiveFields(jsonNode);
            return objectMapper.writeValueAsString(jsonNode);
        } catch (Exception e) {
            // Jika bukan JSON, return as-is atau masking dengan regex
            return maskPlainTextBody(requestBody);
        }
    }

    /**
     * Mask sensitive data in response body
     */
    public String maskResponseBody(String responseBody) {
        if (!StringUtils.hasText(responseBody)) {
            return responseBody;
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            maskSensitiveFields(jsonNode);
            return objectMapper.writeValueAsString(jsonNode);
        } catch (Exception e) {
            // If not JSON, return as-is
            return responseBody;
        }
    }

    /**
     * Recursively mask sensitive fields in JSON
     */
    private void maskSensitiveFields(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            // Iterate through all fields
            List<String> fieldNames = new ArrayList<>();
            objectNode.fieldNames().forEachRemaining(fieldNames::add);

            for (String fieldName : fieldNames) {
                JsonNode fieldValue = objectNode.get(fieldName);

                if (isSensitiveField(fieldName)) {
                    // Mask sensitive field
                    objectNode.put(fieldName, MASKED_VALUE);
                } else if (fieldValue.isObject() || fieldValue.isArray()) {
                    // Recursively process nested objects/arrays
                    maskSensitiveFields(fieldValue);
                }
            }
        } else if (node.isArray()) {
            for (JsonNode arrayItem : node) {
                maskSensitiveFields(arrayItem);
            }
        }
    }

    /**
     * Check if field is sensitive
     */
    private boolean isSensitiveField(String fieldName) {
        String lowerField = fieldName.toLowerCase();
        return SENSITIVE_FIELDS.stream().anyMatch(lowerField::contains);
    }

    /**
     * Mask sensitive data in plain text (non-JSON) body
     * Menggunakan regex untuk menemukan pola password
     */
    private String maskPlainTextBody(String body) {
        if (!StringUtils.hasText(body)) {
            return body;
        }

        // Pattern untuk "password":"value" atau "password" : "value"
        String masked = body.replaceAll(
                "(?i)(\"password\"\\s*:\\s*\")([^\"]*)(\")",
                "$1" + MASKED_VALUE + "$3"
        );

        // Pattern untuk password=value dalam form data
        masked = masked.replaceAll(
                "(?i)(password=)([^&\\s]*)",
                "$1" + MASKED_VALUE
        );

        // Pattern untuk {"password":"value"} tanpa spasi
        masked = masked.replaceAll(
                "(?i)(\\{\"password\":\")([^\"]*)(\")",
                "$1" + MASKED_VALUE + "$3"
        );

        return masked;
    }

    /**
     * Mask headers yang sensitif
     */
    public java.util.Map<String, String> maskHeaders(java.util.Map<String, String> headers) {
        if (headers == null) {
            return headers;
        }

        java.util.Map<String, String> maskedHeaders = new java.util.HashMap<>();
        for (java.util.Map.Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (isSensitiveHeader(key)) {
                maskedHeaders.put(key, MASKED_VALUE);
            } else {
                maskedHeaders.put(key, value);
            }
        }
        return maskedHeaders;
    }

    /**
     * Check if header is sensitive
     */
    private boolean isSensitiveHeader(String headerName) {
        String lowerHeader = headerName.toLowerCase();
        return lowerHeader.contains("authorization") ||
                lowerHeader.contains("token") ||
                lowerHeader.contains("apikey") ||
                lowerHeader.contains("secret") ||
                lowerHeader.contains("cookie");
    }
}
