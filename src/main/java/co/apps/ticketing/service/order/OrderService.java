package co.apps.ticketing.service.order;

import co.apps.ticketing.dto.orders.OrderRequest;
import co.apps.ticketing.dto.response.BookingResult;
import co.apps.ticketing.dto.response.OrderResponse;
import co.apps.ticketing.entity.Order;
import co.apps.ticketing.entity.Schedule;
import co.apps.ticketing.entity.Seat;
import co.apps.ticketing.enums.OrderStatus;
import co.apps.ticketing.enums.SeatStatus;
import co.apps.ticketing.repository.ScheduleRepository;
import co.apps.ticketing.repository.SeatRepository;
import co.apps.ticketing.repository.order.OrderRepository;
import co.apps.ticketing.service.queue.QueueService;
import co.apps.ticketing.service.seat.SeatLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ScheduleRepository scheduleRepository;
    private final SeatRepository seatRepository;
    private final SeatLockService seatLockService;
    private final QueueService queueService;

    private static final int PAYMENT_TIMEOUT_MINUTES = 15;

    /**
     * Proses pemesanan tiket - dengan Redis Lock + Pessimistic Lock di Database
     */
    @Transactional
    public BookingResult createOrder(OrderRequest request) {
        log.info("Creating order for user: {}, schedule: {}, seats: {}",
                request.getUserId(), request.getScheduleId(), request.getSeatNumbers());

        try {
            // 1. Validasi token antrian (jika menggunakan queue)
            if (request.getIdempotencyKey() != null) {
                boolean tokenValid = queueService.validateToken(
                        request.getIdempotencyKey(),
                        request.getScheduleId(),
                        request.getUserId()
                );

                if (!tokenValid) {
                    return BookingResult.builder()
                            .success(false)
                            .message("Token tidak valid atau sudah kadaluarsa")
                            .errorCode("INVALID_TOKEN")
                            .build();
                }
            }

            // 2. Lock semua kursi menggunakan Redis Distributed Lock
            List<String> lockedSeats = lockAllSeats(request);
            if (lockedSeats.isEmpty()) {
                return BookingResult.builder()
                        .success(false)
                        .message("Beberapa kursi sudah dipesan orang lain")
                        .errorCode("SEATS_TAKEN")
                        .build();
            }

            // 3. Validasi ketersediaan kursi di database (dengan pessimistic lock)
            List<Seat> seats = seatRepository.findByScheduleIdAndSeatNumberIn(
                    request.getScheduleId(),
                    request.getSeatNumbers()
            );

            // Cek apakah semua kursi AVAILABLE
            boolean allAvailable = seats.stream()
                    .allMatch(seat -> SeatStatus.AVAILABLE.name().equals(seat.getStatus()));

            if (!allAvailable) {
                // Rollback Redis locks
                unlockAllSeats(request, lockedSeats);
                return BookingResult.builder()
                        .success(false)
                        .message("Kursi tidak tersedia")
                        .errorCode("SEATS_UNAVAILABLE")
                        .build();
            }

            // 4. Update status kursi menjadi LOCKED di database
            for (Seat seat : seats) {
                int updated = seatRepository.lockSeat(
                        request.getScheduleId(),
                        seat.getSeatNumber(),
                        request.getUserId(),
                        seat.getVersion()
                );

                if (updated == 0) {
                    // Race condition detected
                    unlockAllSeats(request, lockedSeats);
                    return BookingResult.builder()
                            .success(false)
                            .message("Kursi gagal dikunci, silakan coba lagi")
                            .errorCode("LOCK_FAILED")
                            .build();
                }
            }

            // 5. Kurangi available seats di schedule (optimistic locking)
            Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                    .orElseThrow(() -> new RuntimeException("Schedule not found"));

            int updated = scheduleRepository.decrementAvailableSeats(
                    request.getScheduleId(),
                    request.getSeatNumbers().size(),
                    schedule.getVersion()
            );

            if (updated == 0) {
                // Race condition: seat availability changed
                unlockAllSeats(request, lockedSeats);
                return BookingResult.builder()
                        .success(false)
                        .message("Stok tiket habis, silakan coba lagi")
                        .errorCode("STOCK_UPDATED")
                        .build();
            }

            // 6. Buat order dengan status PENDING
            Order order = new Order();
            order.setOrderNumber(generateOrderNumber());
            order.setUserId(request.getUserId());
            order.setScheduleId(request.getScheduleId());
            order.setSeatNumbers(String.join(",", request.getSeatNumbers()));
            order.setTotalSeats(request.getSeatNumbers().size());
            order.setTotalPrice(schedule.getBasePrice() * request.getSeatNumbers().size());
            order.setStatus(OrderStatus.PENDING.name());
            order.setExpireAt(LocalDateTime.now().plusMinutes(PAYMENT_TIMEOUT_MINUTES));
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());
            order.setVersion(0);

            Order savedOrder = orderRepository.save(order);

            log.info("Order created successfully: {}", savedOrder.getOrderNumber());

            // 7. Kirim event ke Kafka untuk notifikasi (opsional)
            // sendOrderCreatedEvent(savedOrder);

            return BookingResult.builder()
                    .success(true)
                    .message("Pesanan berhasil dibuat")
                    .orderResponse(OrderResponse.builder()
                            .orderId(savedOrder.getId())
                            .orderNumber(savedOrder.getOrderNumber())
                            .status(savedOrder.getStatus())
                            .totalPrice(savedOrder.getTotalPrice())
                            .expireAt(savedOrder.getExpireAt())
                            .paymentUrl(generatePaymentUrl(savedOrder.getOrderNumber()))
                            .message("Segera lakukan pembayaran dalam " + PAYMENT_TIMEOUT_MINUTES + " menit")
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("Error creating order", e);
            return BookingResult.builder()
                    .success(false)
                    .message("Terjadi kesalahan sistem: " + e.getMessage())
                    .errorCode("SYSTEM_ERROR")
                    .build();
        }
    }

    /**
     * Lock semua kursi menggunakan Redis
     */
    private List<String> lockAllSeats(OrderRequest request) {
        return request.getSeatNumbers().stream()
                .filter(seatNumber -> seatLockService.lockSeat(
                        request.getScheduleId(),
                        seatNumber,
                        request.getUserId()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Unlock semua kursi (rollback)
     */
    private void unlockAllSeats(OrderRequest request, List<String> lockedSeats) {
        lockedSeats.forEach(seatNumber ->
                seatLockService.unlockSeat(request.getScheduleId(), seatNumber, request.getUserId())
        );
    }

    /**
     * Konfirmasi pembayaran - dengan Optimistic Locking
     */
    @Transactional
    public BookingResult confirmPayment(String orderNumber, Long userId) {
        log.info("Confirming payment for order: {}, user: {}", orderNumber, userId);

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Validasi ownership
        if (!order.getUserId().equals(userId)) {
            return BookingResult.builder()
                    .success(false)
                    .message("Order bukan milik user ini")
                    .errorCode("UNAUTHORIZED")
                    .build();
        }

        // Validasi status
        if (!OrderStatus.PENDING.name().equals(order.getStatus())) {
            return BookingResult.builder()
                    .success(false)
                    .message("Order sudah diproses")
                    .errorCode("INVALID_STATUS")
                    .build();
        }

        // Cek expired
        if (order.getExpireAt().isBefore(LocalDateTime.now())) {
            // Cancel order
            cancelExpiredOrder(order);
            return BookingResult.builder()
                    .success(false)
                    .message("Order sudah kadaluarsa")
                    .errorCode("EXPIRED")
                    .build();
        }

        // Update status order dengan optimistic locking
        int updated = orderRepository.updateOrderStatus(
                order.getId(),
                OrderStatus.PAID.name(),
                OrderStatus.PENDING.name(),
                order.getVersion()
        );

        if (updated == 0) {
            return BookingResult.builder()
                    .success(false)
                    .message("Order sudah diproses oleh sistem lain")
                    .errorCode("CONCURRENT_MODIFICATION")
                    .build();
        }

        // Update status kursi menjadi BOOKED
        String[] seatNumbers = order.getSeatNumbers().split(",");
        for (String seatNumber : seatNumbers) {
            int seatUpdated = seatRepository.bookSeat(
                    order.getScheduleId(),
                    seatNumber,
                    userId
            );

            if (seatUpdated == 0) {
                log.error("Failed to book seat: {}", seatNumber);
                // Rollback order status?
            }

            // Hapus Redis lock
            seatLockService.unlockSeat(order.getScheduleId(), seatNumber, userId);
        }

        log.info("Payment confirmed for order: {}", orderNumber);

        return BookingResult.builder()
                .success(true)
                .message("Pembayaran berhasil, tiket telah dipesan")
                .build();
    }

    /**
     * Cancel expired orders (dijalankan oleh scheduler)
     */
    @Transactional
    public void cancelExpiredOrders() {
        List<Order> expiredOrders = orderRepository.findByStatusAndExpireAtBefore(
                OrderStatus.PENDING.name(),
                LocalDateTime.now()
        );

        for (Order order : expiredOrders) {
            cancelExpiredOrder(order);
        }
    }

    private void cancelExpiredOrder(Order order) {
        log.info("Cancelling expired order: {}", order.getOrderNumber());

        // Update status order
        int updated = orderRepository.updateOrderStatus(
                order.getId(),
                OrderStatus.EXPIRED.name(),
                OrderStatus.PENDING.name(),
                order.getVersion()
        );

        if (updated > 0) {
            // Kembalikan kursi ke AVAILABLE
            String[] seatNumbers = order.getSeatNumbers().split(",");
            for (String seatNumber : seatNumbers) {
                seatRepository.unlockSeat(order.getScheduleId(), seatNumber);
                seatLockService.unlockSeat(order.getScheduleId(), seatNumber, order.getUserId());
            }

            // Kembalikan available seats
            Schedule schedule = scheduleRepository.findById(order.getScheduleId())
                    .orElse(null);
            if (schedule != null) {
                scheduleRepository.incrementAvailableSeats(
                        order.getScheduleId(),
                        order.getTotalSeats(),
                        schedule.getVersion()
                );
            }
        }
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generatePaymentUrl(String orderNumber) {
        // Integrasi dengan payment gateway
        return "https://payment.example.com/pay/" + orderNumber;
    }
}
