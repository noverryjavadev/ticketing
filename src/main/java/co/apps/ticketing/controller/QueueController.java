package co.apps.ticketing.controller;

import co.apps.ticketing.service.queue.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/queue")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;

    /**
     * Get queue size for a schedule (admin only)
     */
    @GetMapping("/size/{scheduleId}")
    public ResponseEntity<Map<String, Long>> getQueueSize(@PathVariable Long scheduleId) {
        // Implement get queue size
        return ResponseEntity.ok(Map.of("size", 0L));
    }

    /**
     * Flush queue (for testing)
     */
    @DeleteMapping("/flush/{scheduleId}")
    public ResponseEntity<Void> flushQueue(@PathVariable Long scheduleId) {
        // Implement flush queue
        return ResponseEntity.ok().build();
    }
}
