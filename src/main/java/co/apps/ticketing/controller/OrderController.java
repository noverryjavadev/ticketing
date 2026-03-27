package co.apps.ticketing.controller;


import co.apps.ticketing.dto.orders.OrderRequest;
import co.apps.ticketing.dto.response.BookingResult;
import co.apps.ticketing.dto.response.OrderResponse;
import co.apps.ticketing.dto.response.QueueStatusResponse;
import co.apps.ticketing.service.order.OrderService;
import co.apps.ticketing.service.queue.QueueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final QueueService queueService;

    // Store SSE emitters for real-time updates
    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * Join antrian untuk schedule tertentu
     */
    @PostMapping("/queue/join")
    public ResponseEntity<QueueStatusResponse> joinQueue(
            @RequestParam Long scheduleId,
            @RequestParam Long userId) {

        Long position = queueService.joinQueue(scheduleId, userId);
        Long waitTime = queueService.estimateWaitTime(scheduleId, position);

        return ResponseEntity.ok(QueueStatusResponse.builder()
                .position(position)
                .estimatedWaitTimeSeconds(waitTime)
                .status("WAITING")
                .build());
    }

    /**
     * Cek status antrian
     */
    @GetMapping("/queue/status")
    public ResponseEntity<QueueStatusResponse> getQueueStatus(
            @RequestParam Long scheduleId,
            @RequestParam Long userId) {

        Long position = queueService.getQueuePosition(scheduleId, userId);

        if (position == null) {
            return ResponseEntity.ok(QueueStatusResponse.builder()
                    .status("NOT_IN_QUEUE")
                    .build());
        }

        Long waitTime = queueService.estimateWaitTime(scheduleId, position);

        return ResponseEntity.ok(QueueStatusResponse.builder()
                .position(position)
                .estimatedWaitTimeSeconds(waitTime)
                .status("WAITING")
                .build());
    }

    /**
     * Leave queue
     */
    @DeleteMapping("/queue/leave")
    public ResponseEntity<Void> leaveQueue(
            @RequestParam Long scheduleId,
            @RequestParam Long userId) {

        queueService.leaveQueue(scheduleId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Create order (booking)
     * Endpoint ini hanya bisa diakses setelah mendapatkan token dari queue
     */
    @PostMapping("/create")
    public ResponseEntity<BookingResult> createOrder(@Valid @RequestBody OrderRequest request) {
        BookingResult result = orderService.createOrder(request);

        if (result.getSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Confirm payment
     */
    @PostMapping("/{orderNumber}/confirm-payment")
    public ResponseEntity<BookingResult> confirmPayment(
            @PathVariable String orderNumber,
            @RequestParam Long userId) {

        BookingResult result = orderService.confirmPayment(orderNumber, userId);

        if (result.getSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Get order details
     */
    @GetMapping("/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderNumber) {
        // Implement get order details
        return ResponseEntity.ok().build();
    }

    /**
     * SSE endpoint untuk real-time queue position updates
     */
    @GetMapping("/queue/stream/{userId}/{scheduleId}")
    public SseEmitter streamQueuePosition(
            @PathVariable Long userId,
            @PathVariable Long scheduleId) {

        String emitterId = userId + ":" + scheduleId;
        SseEmitter emitter = new SseEmitter(30000L); // 30 seconds timeout

        emitters.put(emitterId, emitter);

        emitter.onTimeout(() -> {
            emitters.remove(emitterId);
            log.info("SSE timeout for user: {}", userId);
        });

        emitter.onCompletion(() -> {
            emitters.remove(emitterId);
            log.info("SSE completed for user: {}", userId);
        });

        // Send initial position
        try {
            Long position = queueService.getQueuePosition(scheduleId, userId);
            emitter.send(SseEmitter.event()
                    .name("position")
                    .data(position != null ? position : 0));
        } catch (IOException e) {
            log.error("Error sending initial position", e);
            emitter.completeWithError(e);
        }

        return emitter;
    }
}
