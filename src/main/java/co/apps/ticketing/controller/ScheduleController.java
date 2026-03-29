package co.apps.ticketing.controller;

import co.apps.ticketing.dto.request.ScheduleSearchRequest;
import co.apps.ticketing.dto.response.ScheduleDetailResponse;
import co.apps.ticketing.dto.response.ScheduleSearchResponse;
import co.apps.ticketing.dto.response.SeatMapResponse;
import co.apps.ticketing.service.schedule.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    /**
     * Mencari jadwal berdasarkan rute dan tanggal
     * GET /api/schedules/search?from=Jakarta&to=Bandung&date=2024-12-25
     *
     * Ini adalah endpoint utama yang akan digunakan pengguna untuk mencari tiket
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ScheduleSearchResponse>> searchSchedules(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PageableDefault(size = 20, sort = "departureTime") Pageable pageable) {

        ScheduleSearchRequest request = ScheduleSearchRequest.builder()
                .from(from)
                .to(to)
                .date(date)
                .build();

        return ResponseEntity.ok(scheduleService.searchSchedules(request, pageable));
    }

    /**
     * Mendapatkan detail jadwal beserta peta kursi
     * GET /api/schedules/{scheduleId}/seats
     */
    @GetMapping("/{scheduleId}/seats")
    public ResponseEntity<SeatMapResponse> getSeatMap(
            @PathVariable Long scheduleId,
            @RequestParam(required = false) Long userId) {  // Optional untuk lock status

        SeatMapResponse seatMap = scheduleService.getSeatMap(scheduleId, userId);
        return ResponseEntity.ok(seatMap);
    }

    /**
     * Mendapatkan detail jadwal tertentu
     * GET /api/schedules/{scheduleId}
     */
    @GetMapping("/{scheduleId}")
    public ResponseEntity<ScheduleDetailResponse> getScheduleDetail(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(scheduleService.getScheduleDetail(scheduleId));
    }
}
