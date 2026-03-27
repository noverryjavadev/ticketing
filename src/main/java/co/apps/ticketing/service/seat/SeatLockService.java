package co.apps.ticketing.service.seat;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatLockService {

    private final StringRedisTemplate redisTemplate;
    private static final String LOCK_PREFIX = "lock:seat:";
    private static final long LOCK_DURATION = 5; // 5 minutes

    /**
     * Mengunci kursi menggunakan Redis Distributed Lock
     * @return true jika berhasil mengunci, false jika sudah terkunci orang lain
     */
    public boolean lockSeat(Long scheduleId, String seatNumber, Long userId) {
        String lockKey = LOCK_PREFIX + scheduleId + ":" + seatNumber;
        String lockValue = userId.toString();

        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, LOCK_DURATION, TimeUnit.MINUTES);

        if (Boolean.TRUE.equals(success)) {
            log.info("Seat {} locked by user {}", seatNumber, userId);
            return true;
        }

        String currentOwner = redisTemplate.opsForValue().get(lockKey);
        if (lockValue.equals(currentOwner)) {
            redisTemplate.expire(lockKey, LOCK_DURATION, TimeUnit.MINUTES);
            return true;
        }

        log.warn("Seat {} already locked by {}", seatNumber, currentOwner);
        return false;
    }

    /**
     * Membuka kunci kursi
     */
    public void unlockSeat(Long scheduleId, String seatNumber, Long userId) {
        String lockKey = LOCK_PREFIX + scheduleId + ":" + seatNumber;
        String currentOwner = redisTemplate.opsForValue().get(lockKey);

        if (userId.toString().equals(currentOwner)) {
            redisTemplate.delete(lockKey);
            log.info("Seat {} unlocked by user {}", seatNumber, userId);
        }
    }

    /**
     * Mendapatkan owner lock (untuk debugging)
     */
    public Long getLockOwner(Long scheduleId, String seatNumber) {
        String lockKey = LOCK_PREFIX + scheduleId + ":" + seatNumber;
        String owner = redisTemplate.opsForValue().get(lockKey);
        return owner != null ? Long.parseLong(owner) : null;
    }
}
