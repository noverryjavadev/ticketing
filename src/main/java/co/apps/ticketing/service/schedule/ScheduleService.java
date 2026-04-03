package co.apps.ticketing.service.schedule;

import co.apps.ticketing.dto.request.ScheduleSearchRequest;
import co.apps.ticketing.dto.response.ScheduleDetailResponse;
import co.apps.ticketing.dto.response.ScheduleSearchResponse;
import co.apps.ticketing.dto.response.SeatLayout;
import co.apps.ticketing.dto.response.SeatMapResponse;
import co.apps.ticketing.dto.response.SeatSummary;
import co.apps.ticketing.entity.Schedule;
import co.apps.ticketing.entity.Seat;
import co.apps.ticketing.repository.ScheduleRepository;
import co.apps.ticketing.repository.SeatRepository;
import co.apps.ticketing.service.seat.SeatLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final SeatRepository seatRepository;
    private final SeatLockService seatLockService;

    /**
     * Pencarian jadwal dengan filter
     */
    public Page<ScheduleSearchResponse> searchSchedules(ScheduleSearchRequest request, Pageable pageable) {
        LocalDateTime startOfDay = request.getDate().atStartOfDay();
        LocalDateTime endOfDay = request.getDate().plusDays(1).atStartOfDay();

        List<Schedule> schedules = scheduleRepository.findByRouteFromAndRouteToAndDepartureTimeBetween(
                request.getFrom(),
                request.getTo(),
                startOfDay,
                endOfDay
        );

        List<ScheduleSearchResponse> responses = schedules.stream()
                .map(this::toSearchResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, responses.size());
    }

    private ScheduleSearchResponse toSearchResponse(Schedule schedule) {
        return ScheduleSearchResponse.builder()
                .scheduleId(schedule.getId())
                .busName("")
                .busType("")
                .from(schedule.getRouteFrom())
                .to(schedule.getRouteTo())
                .departureTime(schedule.getDepartureTime())
                .arrivalTime(schedule.getArrivalTime())
                .price(schedule.getBasePrice())
                .availableSeats(schedule.getAvailableSeats())
                .totalSeats(schedule.getTotalSeats())
                .duration(calculateDuration(schedule.getDepartureTime(), schedule.getArrivalTime()))
                .isPromo(checkPromo(schedule))
                .build();
    }

    /**
     * Mendapatkan peta kursi dengan status real-time
     */
    public SeatMapResponse getSeatMap(Long scheduleId, Long userId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        List<Seat> seats = seatRepository.findByScheduleId(scheduleId);

        // Bangun peta kursi
        SeatLayout.SeatLayoutBuilder layoutBuilder = SeatLayout.builder();
        Map<String, String> seatStatusMap = new HashMap<>();
        Map<String, Double> seatPriceMap = new HashMap<>();

        for (Seat seat : seats) {
            String status = seat.getStatus();

            // Jika kursi sedang di-lock, cek apakah lock milik user ini
            if ("LOCKED".equals(status) && userId != null) {
                Long lockOwner = seatLockService.getLockOwner(scheduleId, seat.getSeatNumber());
                if (userId.equals(lockOwner)) {
                    status = "LOCKED_BY_ME";  // User bisa melihat lock miliknya sendiri
                }
            }

            seatStatusMap.put(seat.getSeatNumber(), status);
            seatPriceMap.put(seat.getSeatNumber(), seat.getPrice() != null ?
                    seat.getPrice() : schedule.getBasePrice());
        }

        // Hitung ringkasan
        long available = seats.stream().filter(s -> "AVAILABLE".equals(s.getStatus())).count();
        long locked = seats.stream().filter(s -> "LOCKED".equals(s.getStatus())).count();
        long booked = seats.stream().filter(s -> "BOOKED".equals(s.getStatus())).count();

        return SeatMapResponse.builder()
                .scheduleId(scheduleId)
                .busName(schedule.getBusName())
                .from(schedule.getRouteFrom())
                .to(schedule.getRouteTo())
                .departureTime(schedule.getDepartureTime())
                .basePrice(schedule.getBasePrice())
                .seatLayout(SeatLayout.builder()
                        .seatStatusMap(seatStatusMap)
                        .seatPriceMap(seatPriceMap)
                        .build())
                .summary(SeatSummary.builder()
                        .totalSeats(seats.size())
                        .availableSeats((int) available)
                        .lockedSeats((int) locked)
                        .bookedSeats((int) booked)
                        .build())
                .build();
    }

    private String calculateDuration(LocalDateTime start, LocalDateTime end) {
        long hours = java.time.Duration.between(start, end).toHours();
        long minutes = java.time.Duration.between(start, end).toMinutes() % 60;
        return String.format("%02d:%02d", hours, minutes);
    }

    private Boolean checkPromo(Schedule schedule) {
        // Logika promo: misal diskon untuk jadwal yang akan berangkat dalam 3 hari
        return schedule.getDepartureTime().isBefore(LocalDateTime.now().plusDays(3));
    }

    public ScheduleDetailResponse getScheduleDetail(Long scheduleId) {
        return ScheduleDetailResponse.builder().build();
    }
}
