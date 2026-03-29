package co.apps.ticketing.repository;

import co.apps.ticketing.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    @Modifying
    @Transactional
    @Query(value = "UPDATE schedules SET available_seats = available_seats - :totalSeats, version = version + 1 " +
            "WHERE id = :scheduleId AND available_seats >= :totalSeats AND version = :version",
            nativeQuery = true)
    int decrementAvailableSeats(@Param("scheduleId") Long scheduleId,
                                @Param("totalSeats") Integer totalSeats,
                                @Param("version") Integer version);

    @Modifying
    @Transactional
    @Query(value = "UPDATE schedules SET available_seats = available_seats + :totalSeats, version = version + 1 " +
            "WHERE id = :scheduleId AND version = :version",
            nativeQuery = true)
    int incrementAvailableSeats(@Param("scheduleId") Long scheduleId,
                                @Param("totalSeats") Integer totalSeats,
                                @Param("version") Integer version);

    // Pencarian jadwal berdasarkan rute dan rentang waktu
    List<Schedule> findByRouteFromAndRouteToAndDepartureTimeBetween(
            String from,
            String to,
            LocalDateTime start,
            LocalDateTime end
    );

    // Pencarian dengan multiple filter (lebih kompleks)
    @Query("SELECT s FROM Schedule s WHERE " +
            "(:from IS NULL OR s.routeFrom LIKE %:from%) AND " +
            "(:to IS NULL OR s.routeTo LIKE %:to%) AND " +
            "(:date IS NULL OR DATE(s.departureTime) = :date) AND " +
            "(:busType IS NULL OR s.busType = :busType)")
    List<Schedule> searchSchedules(
            @Param("from") String from,
            @Param("to") String to,
            @Param("date") LocalDate date,
            @Param("busType") String busType
    );

    // Untuk autocomplete
    @Query("SELECT DISTINCT s.routeFrom FROM Schedule s")
    List<String> findAllDistinctOrigins();

    @Query("SELECT DISTINCT s.routeTo FROM Schedule s WHERE s.routeFrom = :from")
    List<String> findDestinationsByOrigin(@Param("from") String from);
}
