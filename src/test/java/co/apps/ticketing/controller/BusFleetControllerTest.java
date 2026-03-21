package co.apps.ticketing.controller;

import co.apps.ticketing.config.JwtAuthenticationFilter;
import co.apps.ticketing.config.JwtService;
import co.apps.ticketing.dto.busfleet.AvailabilityUpdateRequest;
import co.apps.ticketing.dto.busfleet.BusFleetList;
import co.apps.ticketing.dto.busfleet.CustomPagination;
import co.apps.ticketing.dto.busfleet.FleetDataUpdateRequest;
import co.apps.ticketing.dto.busfleet.RegisterBusData;
import co.apps.ticketing.service.busfleet.BusFleetService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = BusFleetController.class,
        excludeAutoConfiguration = {
                DataJpaRepositoriesAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
public class BusFleetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BusFleetService busFleetService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void testGetAllBus() throws Exception {
        CustomPagination<BusFleetList> mockResponse = new CustomPagination<>();

        Mockito.when(busFleetService.getAllBusFleet(Mockito.any()))
                .thenReturn(mockResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/bus-fleet")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateBusData() throws Exception {
        RegisterBusData request = new RegisterBusData();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/bus-fleet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("00"));
    }

    @Test
    void testUpdateAvailability() throws Exception {
        AvailabilityUpdateRequest request = new AvailabilityUpdateRequest();

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/bus-fleet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateFleetData() throws Exception {
        FleetDataUpdateRequest request = new FleetDataUpdateRequest();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/bus-fleet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteFleetData() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/bus-fleet/{id}", 1))
                .andExpect(status().isOk());
    }
}
