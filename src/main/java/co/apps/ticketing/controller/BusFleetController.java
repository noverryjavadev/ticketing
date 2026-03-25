package co.apps.ticketing.controller;

import co.apps.ticketing.dto.PaginationRequest;
import co.apps.ticketing.dto.busfleet.AvailabilityUpdateRequest;
import co.apps.ticketing.dto.busfleet.BusFleetList;
import co.apps.ticketing.dto.busfleet.CustomPagination;
import co.apps.ticketing.dto.busfleet.FleetDataUpdateRequest;
import co.apps.ticketing.dto.busfleet.RegisterBusData;
import co.apps.ticketing.dto.response.BaseResponse;
import co.apps.ticketing.service.busfleet.BusFleetService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Description;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/bus-fleet")
@RequiredArgsConstructor
@Tag(name = "Management Bus Fleet")
public class BusFleetController {

    private final BusFleetService busFleetService;

    @GetMapping
    @Description("Get All Bus List")
    public ResponseEntity<?> getAllBus(@RequestParam(value = "page") int page,
                                       @RequestParam(value = "size") int size){
        if (page < 1){
            page = 1;
        }
        CustomPagination<BusFleetList> busFleetListPage = busFleetService.getAllBusFleet(new PaginationRequest(page - 1, size, "id", "ASC"));
        return ResponseEntity.ok(busFleetListPage);
    }

    @PostMapping
    public ResponseEntity<?> createBusData(@RequestBody RegisterBusData registerBusData){
        busFleetService.createBusData(registerBusData);
        return ResponseEntity.ok(BaseResponse.builder()
                        .status("00")
                        .message("create success")
                .build());
    }

    @PatchMapping
    public ResponseEntity<?> updateAvailability(@RequestBody AvailabilityUpdateRequest request){
        busFleetService.updateAvailability(request);
        return ResponseEntity.ok(BaseResponse.builder()
                .status("00")
                .message("patch success")
                .build());
    }

    @PutMapping
    public ResponseEntity<?> updateBusFleetData(@RequestBody FleetDataUpdateRequest request){
        busFleetService.updateFleetData(request);
        return ResponseEntity.ok(BaseResponse.builder()
                .status("00")
                .message("update success")
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFleetData(@PathVariable("id") Long id){
        busFleetService.deleteFleetData(id);
        return ResponseEntity.ok(BaseResponse.builder()
                .status("00")
                .message("delete success")
                .build());
    }


}
