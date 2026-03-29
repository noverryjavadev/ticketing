package co.apps.ticketing.repository;

import co.apps.ticketing.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByScheduleIdAndSeatNumberIn(Long scheduleId, List<String> seatNumbers);

    Optional<Seat> findByScheduleIdAndSeatNumber(Long scheduleId, String seatNumber);

    List<Seat> findByScheduleId(Long scheduleId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE seats SET status = 'LOCKED', locked_by = :userId, locked_at = NOW(), version = version + 1 " +
            "WHERE schedule_id = :scheduleId AND seat_number = :seatNumber AND status = 'AVAILABLE' AND version = :version",
            nativeQuery = true)
    int lockSeat(@Param("scheduleId") Long scheduleId,
                 @Param("seatNumber") String seatNumber,
                 @Param("userId") Long userId,
                 @Param("version") Integer version);

    @Modifying
    @Transactional
    @Query(value = "UPDATE seats SET status = 'BOOKED', locked_by = NULL, locked_at = NULL, version = version + 1 " +
            "WHERE schedule_id = :scheduleId AND seat_number = :seatNumber AND status = 'LOCKED' AND locked_by = :userId",
            nativeQuery = true)
    int bookSeat(@Param("scheduleId") Long scheduleId,
                 @Param("seatNumber") String seatNumber,
                 @Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE seats SET status = 'AVAILABLE', locked_by = NULL, locked_at = NULL, version = version + 1 " +
            "WHERE schedule_id = :scheduleId AND seat_number = :seatNumber AND status = 'LOCKED'",
            nativeQuery = true)
    int unlockSeat(@Param("scheduleId") Long scheduleId,
                   @Param("seatNumber") String seatNumber);
}
