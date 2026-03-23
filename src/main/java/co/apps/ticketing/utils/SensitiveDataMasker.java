package co.apps.ticketing.utils;

import co.apps.ticketing.anotation.Sensitive;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Component
public class SensitiveDataMasker {

    private final ObjectMapper objectMapper;

    public SensitiveDataMasker() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Mask sensitive fields in an object using reflection
     */
    public <T> T maskSensitiveFields(T object) {
        if (object == null) {
            return null;
        }

        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Sensitive.class)) {
                field.setAccessible(true);
                try {
                    Sensitive sensitive = field.getAnnotation(Sensitive.class);
                    field.set(object, sensitive.value());
                } catch (IllegalAccessException e) {
                    // Handle exception
                }
            }
        }
        return object;
    }

    /**
     * Mask sensitive fields and convert to JSON
     */
    public String maskAndToJson(Object object) {
        try {
            Object maskedObject = maskSensitiveFields(object);
            return objectMapper.writeValueAsString(maskedObject);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
