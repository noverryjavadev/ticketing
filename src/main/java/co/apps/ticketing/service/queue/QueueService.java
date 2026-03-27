package co.apps.ticketing.service.queue;

import co.apps.ticketing.service.seat.SeatLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService {

    private final StringRedisTemplate redisTemplate;
    private final SeatLockService seatLockService;
    private static final String QUEUE_PREFIX = "queue:schedule:";
    private static final String TOKEN_PREFIX = "token:";
    private static final int BATCH_SIZE = 50; // Jumlah user yang diproses per batch
    private static final int TOKEN_EXPIRE_MINUTES = 5;

    /**
     * Join queue untuk schedule tertentu
     */
    public Long joinQueue(Long scheduleId, Long userId) {
        String queueKey = QUEUE_PREFIX + scheduleId;
        long timestamp = System.currentTimeMillis();

        // Gunakan ZADD dengan timestamp sebagai score (FIFO)
        Boolean added = redisTemplate.opsForZSet()
                .add(queueKey, userId.toString(), timestamp);

        if (Boolean.TRUE.equals(added)) {
            // Dapatkan posisi
            Long position = redisTemplate.opsForZSet()
                    .rank(queueKey, userId.toString());

            log.info("User {} joined queue for schedule {}, position: {}",
                    userId, scheduleId, position);
            return position != null ? position + 1 : null;
        }

        return null;
    }

    /**
     * Cek posisi antrian
     */
    public Long getQueuePosition(Long scheduleId, Long userId) {
        String queueKey = QUEUE_PREFIX + scheduleId;
        Long position = redisTemplate.opsForZSet()
                .rank(queueKey, userId.toString());

        return position != null ? position + 1 : null;
    }

    /**
     * Perkiraan waktu tunggu (dalam detik)
     */
    public Long estimateWaitTime(Long scheduleId, Long position) {
        if (position == null) return 0L;

        // Asumsi: 50 user diproses per 10 detik
        long processRate = BATCH_SIZE;
        long intervalSeconds = 10;

        long batchesNeeded = (long) Math.ceil((double) position / processRate);
        return batchesNeeded * intervalSeconds;
    }

    /**
     * Scheduler untuk memproses antrian
     * Berjalan setiap 10 detik
     */
    @Scheduled(fixedDelay = 10000) // 10 detik
    public void processQueue() {
        // Dapatkan semua schedule yang sedang aktif
        Set<String> keys = redisTemplate.keys(QUEUE_PREFIX + "*");

        if (keys != null) {
            keys.forEach(this::processQueueForSchedule);
        }
    }

    private void processQueueForSchedule(String queueKey) {
        Long scheduleId = extractScheduleId(queueKey);

        // Ambil BATCH_SIZE user teratas
        Set<ZSetOperations.TypedTuple<String>> users =
                redisTemplate.opsForZSet()
                        .popMin(queueKey, BATCH_SIZE);

        if (users != null && !users.isEmpty()) {
            log.info("Processing {} users from queue for schedule {}",
                    users.size(), scheduleId);

            users.forEach(user -> {
                Long userId = Long.parseLong(user.getValue());
                String token = generateToken();

                // Simpan token dengan TTL
                String tokenKey = TOKEN_PREFIX + token;
                redisTemplate.opsForValue()
                        .set(tokenKey, userId.toString(),
                                Duration.ofMinutes(TOKEN_EXPIRE_MINUTES));

                // Simpan mapping user ke token (untuk notifikasi)
                String userTokenKey = "user:token:" + scheduleId + ":" + userId;
                redisTemplate.opsForValue()
                        .set(userTokenKey, token,
                                Duration.ofMinutes(TOKEN_EXPIRE_MINUTES));

                log.info("User {} admitted with token {} for schedule {}",
                        userId, token, scheduleId);

                // TODO: Kirim notifikasi ke user via SSE/WebSocket
            });
        }
    }

    /**
     * Validasi token akses
     */
    public boolean validateToken(String token, Long scheduleId, Long userId) {
        String tokenKey = TOKEN_PREFIX + token;
        String storedUserId = redisTemplate.opsForValue().get(tokenKey);

        if (storedUserId != null && storedUserId.equals(userId.toString())) {
            // Hapus token setelah digunakan (one-time use)
            redisTemplate.delete(tokenKey);
            return true;
        }

        return false;
    }

    /**
     * Leave queue (jika user membatalkan)
     */
    public void leaveQueue(Long scheduleId, Long userId) {
        String queueKey = QUEUE_PREFIX + scheduleId;
        redisTemplate.opsForZSet().remove(queueKey, userId.toString());
        log.info("User {} left queue for schedule {}", userId, scheduleId);
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private Long extractScheduleId(String queueKey) {
        return Long.parseLong(queueKey.replace(QUEUE_PREFIX, ""));
    }
}
