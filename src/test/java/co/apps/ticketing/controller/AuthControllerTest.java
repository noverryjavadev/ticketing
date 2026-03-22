package co.apps.ticketing.controller;

import co.apps.ticketing.config.JwtAuthenticationFilter;
import co.apps.ticketing.config.JwtService;
import co.apps.ticketing.dto.AuthenticationRequest;
import co.apps.ticketing.dto.AuthenticationResponse;
import co.apps.ticketing.dto.RegisterRequest;
import co.apps.ticketing.service.AuthenticationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
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
        controllers = AuthController.class
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;



    @Test
    void testRegister() {
        AuthenticationResponse authenticationResponse = new AuthenticationResponse();

        Mockito.when(authenticationService.register(RegisterRequest.builder().build())).thenReturn(authenticationResponse);

        Assertions.assertNotNull(authenticationResponse);
    }

    @Test
    void testRegisterSuccess() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@mail.com")
                .password("123456")
                .build();

        AuthenticationResponse response = new AuthenticationResponse();
        response.setAccessToken("dummy-token");

        Mockito.when(authenticationService.register(Mockito.any()))
                .thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("dummy-token"));
    }

    @Test
    void testAuthenticate() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmail("test@mail.com");
        request.setPassword("123456");

        AuthenticationResponse response = new AuthenticationResponse();
        response.setAccessToken("auth-token");

        Mockito.when(authenticationService.authenticate(Mockito.any()))
                .thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("auth-token"));
    }

    @Test
    void testRefreshToken() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/refresh-token"))
                .andExpect(status().isOk());

        Mockito.verify(authenticationService, Mockito.times(1))
                .refreshToken(Mockito.any(), Mockito.any());
    }
}
