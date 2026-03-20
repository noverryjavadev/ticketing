package co.apps.ticketing.repository;

import co.apps.ticketing.entity.BusFleet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusFleetRepository extends JpaRepository<BusFleet, Long> {

    Optional<BusFleet> findByRegNumber(String regNumber);
}
