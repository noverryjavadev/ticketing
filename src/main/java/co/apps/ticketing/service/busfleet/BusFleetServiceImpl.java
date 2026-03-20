package co.apps.ticketing.service.busfleet;

import co.apps.ticketing.dto.PaginationRequest;
import co.apps.ticketing.dto.busfleet.AvailabilityUpdateRequest;
import co.apps.ticketing.dto.busfleet.BusFleetList;
import co.apps.ticketing.dto.busfleet.CustomPagination;
import co.apps.ticketing.dto.busfleet.FleetDataUpdateRequest;
import co.apps.ticketing.dto.busfleet.RegisterBusData;
import co.apps.ticketing.entity.BusFleet;
import co.apps.ticketing.repository.BusFleetRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BusFleetServiceImpl implements BusFleetService {

    private final BusFleetRepository busFleetRepository;


    @Override
    public CustomPagination<BusFleetList> getAllBusFleet(PaginationRequest request) {

        Sort.Direction direction = request.sortDirection().equalsIgnoreCase("DESC")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(
                request.page(),
                request.size(),
                Sort.by(direction, request.orderBy())
        );

        Page<BusFleet> busFleetPage = busFleetRepository.findAll(pageable);
        List<BusFleetList> content = busFleetPage.getContent()
                .stream()
                .map(this::convertToBusFleetList)
                .toList();

        return new CustomPagination<>(
                busFleetPage.getNumber(),           // page (halaman saat ini, 0-based)
                busFleetPage.getSize(),              // size (jumlah data per halaman)
                busFleetPage.getTotalPages(),        // totalPages (total halaman)
                busFleetPage.getTotalElements(),     // totalElements (total seluruh data)
                content.size(),                      // numberOfElements (jumlah data di halaman ini)
                content                              // content (list data)
        );
    }

    private BusFleetList convertToBusFleetList(BusFleet busFleet) {
        return BusFleetList.builder()
                .id(busFleet.getId())
                .regNumber(busFleet.getRegNumber())
                .busBrand(busFleet.getBusBrand())
                .busType(busFleet.getBusType())
                .numberOfSeat(busFleet.getNumberOfSeat())
                .availability(busFleet.isAvailability())
                .fleetStatus(busFleet.getFleetStatus())
                .build();
    }

    @Override
    public void createBusData(RegisterBusData registerBusData) {

        if (registerBusData == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "request not present");
        }

        try{
            BusFleet busFleet = BusFleet.builder()
                    .regNumber(registerBusData.getRegNumber())
                    .busBrand(registerBusData.getBusBrand())
                    .busType(registerBusData.getBusType())
                    .numberOfSeat(registerBusData.getNumOfSeat())
                    .availability(false)
                    .fleetStatus(registerBusData.getStatus())
                    .description(registerBusData.getDesc())
                    .build();
            busFleetRepository.save(busFleet);
        }catch (Exception e){
            log.error("Error on save : {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Save Error");
        }



    }

    @Override
    public void updateAvailability(AvailabilityUpdateRequest request) {
        if (request == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "request not present");
        }

        try{
            Optional<BusFleet> optionalBusFleet = busFleetRepository.findById(request.getId());
            if (optionalBusFleet.isEmpty()){
                throw new EntityNotFoundException("Bus Data Not Found");
            }

            BusFleet busFleet = optionalBusFleet.get();
            busFleet.setAvailability(request.isAvailable());
            busFleet.setFleetStatus(request.getNewStatus());
            busFleetRepository.save(busFleet);
        }catch (Exception e){
            log.error("Error on save : {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Save Error");
        }
    }

    @Override
    public void updateFleetData(FleetDataUpdateRequest request) {
        if (request == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "request not present");
        }

        try{
            Optional<BusFleet> optionalBusFleet = busFleetRepository.findById(request.getId());
            if (optionalBusFleet.isEmpty()){
                throw new EntityNotFoundException("Bus Data Not Found");
            }

            BusFleet busFleet = optionalBusFleet.get();
            busFleet.setRegNumber(request.getRegNumber());
            busFleet.setNumberOfSeat(request.getNumOfSeat());
            busFleet.setAvailability(request.isAvailable());
            busFleet.setBusType(request.getBusType());
            busFleet.setMaintenanceSchedule(convertToLocalDateTime(request.getMaintenanceSchedule()));
            busFleet.setFleetStatus(request.getStatus());
            busFleet.setDescription(request.getDesc());
            busFleetRepository.save(busFleet);
        }catch (Exception e){
            log.error("Error on save : {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Save Error");
        }
    }

    @Override
    public void deleteFleetData(Long id) {
        Optional<BusFleet> optionalBusFleet = busFleetRepository.findById(id);
        if (optionalBusFleet.isEmpty()){
            throw new EntityNotFoundException("Bus Data Not Found");
        }
        busFleetRepository.delete(optionalBusFleet.get());
    }

    private static LocalDateTime convertToLocalDateTime(String dateString) {
        LocalDate date = LocalDate.parse(dateString);
        return date.atStartOfDay();
    }

    private static LocalDateTime convertWithFormatter(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(dateString, formatter);
        return date.atStartOfDay();
    }
}
