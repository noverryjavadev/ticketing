package co.apps.ticketing.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bus_fleet")
public class BusFleet extends AuditTable{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "registration_number")
    private String regNumber;

    @Column(name = "bus_brand")
    private String busBrand;

    @Column(name = "bus_type")
    private String busType;

    @Column(name = "number_of_seat")
    private Long numberOfSeat;

    @Column(name = "availability")
    private boolean availability;

    @Column(name = "fleet_status")
    private String fleetStatus;

    @Column(name = "maintenance_schedule")
    private LocalDateTime maintenanceSchedule;

    @Column(name = "description")
    private String description;


}
