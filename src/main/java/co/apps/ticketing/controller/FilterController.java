package co.apps.ticketing.controller;

import co.apps.ticketing.repository.BusFleetRepository;
import co.apps.ticketing.service.schedule.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/api/filters")
@RequiredArgsConstructor
public class FilterController {

    private final ScheduleService scheduleService;
    private final BusFleetRepository busRepository;

//    /**
//     * Mendapatkan semua nilai filter yang tersedia
//     * GET /api/filters/options
//     */
//    @GetMapping("/options")
//    public ResponseEntity<FilterOptionsResponse> getFilterOptions() {
//        return ResponseEntity.ok(FilterOptionsResponse.builder()
//                .busTypes(Arrays.asList("AC", "NON_AC", "SLEEPER", "VIP"))
//                .priceRanges(Arrays.asList(
//                        new PriceRange(0, 100000),
//                        new PriceRange(100000, 200000),
//                        new PriceRange(200000, 500000)
//                ))
//                .departureTimes(Arrays.asList(
//                        "00:00-06:00",
//                        "06:00-12:00",
//                        "12:00-18:00",
//                        "18:00-24:00"
//                ))
//                .build());
//    }
}
