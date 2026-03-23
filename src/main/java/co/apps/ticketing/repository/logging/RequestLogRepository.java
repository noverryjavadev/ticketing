package co.apps.ticketing.repository.logging;

import co.apps.ticketing.entity.logging.RequestLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RequestLogRepository extends MongoRepository<RequestLog, String> {
    List<RequestLog> findByUrlContaining(String url);
    List<RequestLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    void deleteByTimestampBefore(LocalDateTime date);
}
