package co.apps.ticketing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDetailResponse {
    private Long scheduleId;
//    private BusInfo busInfo;
//    private RouteInfo routeInfo;
    private ScheduleInfo scheduleInfo;
    private PricingInfo pricingInfo;
//    private AvailabilityInfo availabilityInfo;
}

