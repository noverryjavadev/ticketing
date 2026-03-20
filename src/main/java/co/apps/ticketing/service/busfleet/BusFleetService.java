package co.apps.ticketing.service.busfleet;

import co.apps.ticketing.dto.PaginationRequest;
import co.apps.ticketing.dto.busfleet.AvailabilityUpdateRequest;
import co.apps.ticketing.dto.busfleet.BusFleetList;
import co.apps.ticketing.dto.busfleet.CustomPagination;
import co.apps.ticketing.dto.busfleet.FleetDataUpdateRequest;
import co.apps.ticketing.dto.busfleet.RegisterBusData;

public interface BusFleetService {

    CustomPagination<BusFleetList> getAllBusFleet(PaginationRequest request);

    void createBusData(RegisterBusData registerBusData);

    void updateAvailability(AvailabilityUpdateRequest request);

    void updateFleetData(FleetDataUpdateRequest request);

    void deleteFleetData(Long id);
}
