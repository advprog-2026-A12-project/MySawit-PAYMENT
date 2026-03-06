package id.ac.ui.cs.advprog.mysawitpayment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.mysawitpayment.dto.request.RejectPayrollRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.security.JwtFilter;
import id.ac.ui.cs.advprog.mysawitpayment.service.PayrollService;

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
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(
        controllers = PayrollController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtFilter.class
        )
)
class PayrollControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PayrollService payrollService;

    @Autowired
    private ObjectMapper objectMapper;

    private PayrollResponse mockResponse() {
        PayrollResponse response = new PayrollResponse();
        response.setId(UUID.randomUUID());
        response.setAmount(BigDecimal.valueOf(100000));
        response.setKilogram(BigDecimal.valueOf(50));
        response.setRatePerKg(BigDecimal.valueOf(2000));
        response.setMultiplier(BigDecimal.ONE);
        response.setStatus("PENDING");
        response.setReferenceType("HARVEST");
        response.setDescription("Test payroll");
        response.setCreatedAt(OffsetDateTime.now());
        return response;
    }

    @Test
    void getAllPayrollsSuccess() throws Exception {
        Page<PayrollResponse> page =
                new PageImpl<>(List.of(mockResponse()), PageRequest.of(0,20),1);

        when(payrollService.getAllPayrolls(any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/payrolls")
                        .requestAttr("userRole", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Payrolls retrieved successfully"));
    }

    @Test
    void getAllPayrollsFail() {

        ServletException exception = assertThrows(ServletException.class, () ->
                mockMvc.perform(get("/api/v1/payrolls")
                                .requestAttr("userRole", "BURUH"))
                        .andReturn()
        );

        assertInstanceOf(RuntimeException.class, exception.getCause());
        assertEquals("Forbidden", exception.getCause().getMessage());
    }

    @Test
    void getMyPayrollsSuccess() throws Exception {
        UUID userId = UUID.randomUUID();
        Page<PayrollResponse> page =
                new PageImpl<>(List.of(mockResponse()), PageRequest.of(0,20),1);

        when(payrollService.getMyPayrolls(any(UUID.class), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/payrolls/me")
                        .requestAttr("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("My payrolls retrieved successfully"));
    }

    @Test
    void getPayrollByIdSuccess() throws Exception {
        UUID payrollId = UUID.randomUUID();

        when(payrollService.getPayrollById(payrollId))
                .thenReturn(mockResponse());

        mockMvc.perform(get("/api/v1/payrolls/" + payrollId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Payroll detail retrieved successfully"));
    }

    @Test
    void acceptPayrollSuccess() throws Exception {
        UUID payrollId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        when(payrollService.approvePayroll(any(UUID.class), any(UUID.class)))
                .thenReturn(mockResponse());

        mockMvc.perform(put("/api/v1/payrolls/" + payrollId + "/accept")
                        .requestAttr("userId", adminId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Payroll accepted and disbursed successfully"));
    }

    @Test
    void rejectPayrollSuccess() throws Exception {
        UUID payrollId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        RejectPayrollRequest request = new RejectPayrollRequest();
        request.setRejectionReason("Invalid data");

        when(payrollService.rejectPayroll(any(UUID.class), any(UUID.class), anyString()))
                .thenReturn(mockResponse());

        mockMvc.perform(put("/api/v1/payrolls/" + payrollId + "/reject")
                        .requestAttr("userId", adminId.toString())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Payroll rejected"));
    }
}