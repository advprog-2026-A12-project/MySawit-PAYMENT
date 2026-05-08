package id.ac.ui.cs.advprog.mysawitpayment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.CreateWageConfigResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.CurrentWageConfigResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.HistoryWageConfigResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PreviousWageConfigResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.UpdatedByResponse;
import id.ac.ui.cs.advprog.mysawitpayment.exception.ForbiddenException;
import id.ac.ui.cs.advprog.mysawitpayment.security.AuthenticatedUser;
import id.ac.ui.cs.advprog.mysawitpayment.security.JwtFilter;
import id.ac.ui.cs.advprog.mysawitpayment.service.WageConfigService;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(
        controllers = WageConfigController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtFilter.class
        )
)
class WageConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WageConfigService wageConfigService;

    @Test
    void getActiveWageConfigShouldReturnSuccessWhenRoleIsAdmin() throws Exception {
        UUID adminId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        CurrentWageConfigResponse response = CurrentWageConfigResponse.builder()
                .id(UUID.randomUUID())
                .upahBuruhPerKg(new BigDecimal("3.00"))
                .upahSupirPerKg(new BigDecimal("2.00"))
                .upahMandorPerKg(new BigDecimal("1.50"))
                .currency("SawitDollar")
                .isActive(true)
                .updatedBy(UpdatedByResponse.builder()
                        .id(adminId)
                        .name("Admin")
                        .build())
                .effectiveFrom(now)
                .createdAt(now)
                .build();

        when(wageConfigService.getCurrentWageConfig(any(AuthenticatedUser.class))).thenReturn(response);

        mockMvc.perform(get("/api/v1/wage-configs/active")
                        .requestAttr("userId", adminId.toString())
                        .requestAttr("userRole", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Active wage config retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(response.getId().toString()))
                .andExpect(jsonPath("$.data.upahBuruhPerKg").value(3.00))
                .andExpect(jsonPath("$.data.upahSupirPerKg").value(2.00))
                .andExpect(jsonPath("$.data.upahMandorPerKg").value(1.50))
                .andExpect(jsonPath("$.data.currency").value("SawitDollar"))
                .andExpect(jsonPath("$.data.isActive").value(true))
                .andExpect(jsonPath("$.data.updatedBy.id").value(adminId.toString()))
                .andExpect(jsonPath("$.data.updatedBy.name").value("Admin"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(wageConfigService).getCurrentWageConfig(any(AuthenticatedUser.class));
    }

    @Test
    void createNewWageConfigShouldReturnSuccessWhenRequestValidAndRoleIsAdmin() throws Exception {
        UUID adminId = UUID.randomUUID();
        UUID configId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        String requestBody = """
                {
                  "upahBuruhPerKg": 3.00,
                  "upahSupirPerKg": 2.00,
                  "upahMandorPerKg": 1.50
                }
                """;

        CreateWageConfigResponse response = CreateWageConfigResponse.builder()
                .id(configId)
                .upahBuruhPerKg(new BigDecimal("3.00"))
                .upahSupirPerKg(new BigDecimal("2.00"))
                .upahMandorPerKg(new BigDecimal("1.50"))
                .currency("SawitDollar")
                .isActive(true)
                .previousConfig(PreviousWageConfigResponse.builder()
                        .id(UUID.randomUUID())
                        .upahBuruhPerKg(new BigDecimal("2.50"))
                        .upahSupirPerKg(new BigDecimal("1.50"))
                        .upahMandorPerKg(new BigDecimal("1.00"))
                        .deactivatedAt(now)
                        .build())
                .updatedBy(UpdatedByResponse.builder()
                        .id(adminId)
                        .name("Admin")
                        .build())
                .effectiveFrom(now)
                .createdAt(now)
                .build();

        when(wageConfigService.createWageConfig(any(), any(AuthenticatedUser.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/wage-configs")
                        .requestAttr("userRole", "ADMIN")
                        .requestAttr("userId", adminId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Wage config updated successfully"))
                .andExpect(jsonPath("$.data.id").value(configId.toString()))
                .andExpect(jsonPath("$.data.upahBuruhPerKg").value(3.00))
                .andExpect(jsonPath("$.data.upahSupirPerKg").value(2.00))
                .andExpect(jsonPath("$.data.upahMandorPerKg").value(1.50))
                .andExpect(jsonPath("$.data.currency").value("SawitDollar"))
                .andExpect(jsonPath("$.data.isActive").value(true))
                .andExpect(jsonPath("$.data.updatedBy.id").value(adminId.toString()))
                .andExpect(jsonPath("$.data.updatedBy.name").value("Admin"))
                .andExpect(jsonPath("$.data.previousConfig").exists())
                .andExpect(jsonPath("$.timestamp").exists());

        verify(wageConfigService).createWageConfig(any(), any(AuthenticatedUser.class));
    }

    @Test
    void createNewWageConfigShouldReturnBadRequestWhenRequestBodyInvalid() throws Exception {
        UUID adminId = UUID.randomUUID();

        String invalidRequestBody = """
                {
                  "upahBuruhPerKg": 0,
                  "upahSupirPerKg": null,
                  "upahMandorPerKg": -1
                }
                """;

        mockMvc.perform(post("/api/v1/wage-configs")
                        .requestAttr("userRole", "ADMIN")
                        .requestAttr("userId", adminId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isBadRequest());

        verify(wageConfigService, never()).createWageConfig(any(), any(AuthenticatedUser.class));
    }

    @Test
    void getWageConfigHistoryShouldReturnSuccessWhenRoleIsAdmin() throws Exception {
        OffsetDateTime now = OffsetDateTime.now();

        HistoryWageConfigResponse history1 = HistoryWageConfigResponse.builder()
                .id(UUID.randomUUID())
                .upahBuruhPerKg(new BigDecimal("3.00"))
                .upahSupirPerKg(new BigDecimal("2.00"))
                .upahMandorPerKg(new BigDecimal("1.50"))
                .isActive(true)
                .updatedBy(UpdatedByResponse.builder()
                        .id(UUID.randomUUID())
                        .name("Admin")
                        .build())
                .effectiveFrom(now)
                .build();

        HistoryWageConfigResponse history2 = HistoryWageConfigResponse.builder()
                .id(UUID.randomUUID())
                .upahBuruhPerKg(new BigDecimal("2.50"))
                .upahSupirPerKg(new BigDecimal("1.50"))
                .upahMandorPerKg(new BigDecimal("1.00"))
                .isActive(false)
                .updatedBy(UpdatedByResponse.builder()
                        .id(UUID.randomUUID())
                        .name("Admin Lama")
                        .build())
                .effectiveFrom(now.minusDays(1))
                .build();

        Page<HistoryWageConfigResponse> page = new PageImpl<>(
                List.of(history1, history2),
                PageRequest.of(0, 20),
                2
        );

        when(wageConfigService.getWageConfigHistory(any(AuthenticatedUser.class), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/wage-configs/history")
                        .requestAttr("userId", UUID.randomUUID().toString())
                        .requestAttr("userRole", "ADMIN")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Wage config history retrieved successfully"))
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.first").value(true))
                .andExpect(jsonPath("$.data.last").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(history1.getId().toString()))
                .andExpect(jsonPath("$.data.content[0].isActive").value(true))
                .andExpect(jsonPath("$.data.content[1].id").value(history2.getId().toString()))
                .andExpect(jsonPath("$.data.content[1].isActive").value(false))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(wageConfigService).getWageConfigHistory(any(AuthenticatedUser.class), any());
    }

    @Test
    void getActiveWageConfigShouldThrowExceptionWhenRoleIsNotAdmin() {
        when(wageConfigService.getCurrentWageConfig(any(AuthenticatedUser.class)))
                .thenThrow(new ForbiddenException());

        ServletException ex = assertThrows(ServletException.class, () ->
                mockMvc.perform(get("/api/v1/wage-configs/active")
                        .requestAttr("userId", UUID.randomUUID().toString())
                        .requestAttr("userRole", "BURUH"))
        );

        assertNotNull(ex.getCause());
        assertEquals(ForbiddenException.class, ex.getCause().getClass());
        verify(wageConfigService).getCurrentWageConfig(any(AuthenticatedUser.class));
    }

    @Test
    void createNewWageConfigShouldThrowExceptionWhenRoleIsNotAdmin() {
        String requestBody = """
            {
              "upahBuruhPerKg": 3.00,
              "upahSupirPerKg": 2.00,
              "upahMandorPerKg": 1.50
            }
            """;

        when(wageConfigService.createWageConfig(any(), any(AuthenticatedUser.class)))
                .thenThrow(new ForbiddenException());

        ServletException ex = assertThrows(ServletException.class, () ->
                mockMvc.perform(post("/api/v1/wage-configs")
                        .requestAttr("userRole", "BURUH")
                        .requestAttr("userId", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
        );

        assertNotNull(ex.getCause());
        assertEquals(ForbiddenException.class, ex.getCause().getClass());
        verify(wageConfigService).createWageConfig(any(), any(AuthenticatedUser.class));
    }

    @Test
    void getWageConfigHistoryShouldThrowExceptionWhenRoleIsNotAdmin() {
        when(wageConfigService.getWageConfigHistory(any(AuthenticatedUser.class), any()))
                .thenThrow(new ForbiddenException());

        ServletException ex = assertThrows(ServletException.class, () ->
                mockMvc.perform(get("/api/v1/wage-configs/history")
                        .requestAttr("userId", UUID.randomUUID().toString())
                        .requestAttr("userRole", "MANDOR"))
        );

        assertNotNull(ex.getCause());
        assertEquals(ForbiddenException.class, ex.getCause().getClass());
        verify(wageConfigService).getWageConfigHistory(any(AuthenticatedUser.class), any());
    }
}
