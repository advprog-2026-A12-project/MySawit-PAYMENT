package id.ac.ui.cs.advprog.mysawitpayment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.mysawitpayment.dto.request.RejectPayrollRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.request.filter.PayrollFilter;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.AcceptPayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.AdminPayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollApprovedByResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollDetailResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollDisbursementResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollUserResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollWalletResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.RejectPayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.exception.ForbiddenException;
import id.ac.ui.cs.advprog.mysawitpayment.security.AuthenticatedUser;
import id.ac.ui.cs.advprog.mysawitpayment.security.JwtFilter;
import id.ac.ui.cs.advprog.mysawitpayment.service.PayrollService;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    private PayrollUserResponse mockUserResponse() {
        PayrollUserResponse user = new PayrollUserResponse();
        user.setId(UUID.randomUUID());
        user.setRole("BURUH");
        return user;
    }

    private PayrollApprovedByResponse mockApprovedByResponse() {
        PayrollApprovedByResponse approvedBy = new PayrollApprovedByResponse();
        approvedBy.setId(UUID.randomUUID());
        return approvedBy;
    }

    private PayrollResponse mockPayrollResponse() {
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

    private AdminPayrollResponse mockAdminPayrollResponse() {
        AdminPayrollResponse response = new AdminPayrollResponse();
        response.setId(UUID.randomUUID());
        response.setUser(mockUserResponse());
        response.setAmount(BigDecimal.valueOf(100000));
        response.setKilogram(BigDecimal.valueOf(50));
        response.setRatePerKg(BigDecimal.valueOf(2000));
        response.setMultiplier(BigDecimal.ONE);
        response.setStatus("PENDING");
        response.setReferenceType("HARVEST");
        response.setReferenceId(UUID.randomUUID());
        response.setDescription("Test payroll");
        response.setCreatedAt(OffsetDateTime.now());
        return response;
    }

    private PayrollDetailResponse mockPayrollDetailResponse() {
        PayrollDetailResponse response = new PayrollDetailResponse();
        response.setId(UUID.randomUUID());
        response.setUser(mockUserResponse());
        response.setAmount(BigDecimal.valueOf(100000));
        response.setKilogram(BigDecimal.valueOf(50));
        response.setRatePerKg(BigDecimal.valueOf(2000));
        response.setMultiplier(BigDecimal.ONE);
        response.setStatus("PENDING");
        response.setDescription("Test payroll");
        response.setRejectionReason(null);
        response.setReferenceType("HARVEST");
        response.setReferenceId(UUID.randomUUID());
        response.setApprovedBy(mockApprovedByResponse());
        response.setApprovedAt(null);
        response.setCreatedAt(OffsetDateTime.now());
        response.setUpdatedAt(OffsetDateTime.now());
        return response;
    }

    private AcceptPayrollResponse mockAcceptPayrollResponse() {
        PayrollWalletResponse adminWallet = new PayrollWalletResponse();
        adminWallet.setBalanceBefore(BigDecimal.valueOf(500000));
        adminWallet.setBalanceAfter(BigDecimal.valueOf(400000));

        PayrollWalletResponse workerWallet = new PayrollWalletResponse();
        workerWallet.setBalanceBefore(BigDecimal.ZERO);
        workerWallet.setBalanceAfter(BigDecimal.valueOf(100000));

        PayrollDisbursementResponse disbursement = new PayrollDisbursementResponse();
        disbursement.setAdminWallet(adminWallet);
        disbursement.setWorkerWallet(workerWallet);

        AcceptPayrollResponse response = new AcceptPayrollResponse();
        response.setId(UUID.randomUUID());
        response.setUser(mockUserResponse());
        response.setAmount(BigDecimal.valueOf(100000));
        response.setStatus("ACCEPTED");
        response.setApprovedBy(mockApprovedByResponse());
        response.setApprovedAt(OffsetDateTime.now());
        response.setDisbursement(disbursement);
        return response;
    }

    private RejectPayrollResponse mockRejectPayrollResponse() {
        RejectPayrollResponse response = new RejectPayrollResponse();
        response.setId(UUID.randomUUID());
        response.setUser(mockUserResponse());
        response.setAmount(BigDecimal.valueOf(100000));
        response.setStatus("REJECTED");
        response.setRejectionReason("Invalid data");
        response.setApprovedBy(mockApprovedByResponse());
        response.setApprovedAt(OffsetDateTime.now());
        return response;
    }

    @Test
    void getAllPayrollsSuccess() throws Exception {
        Page<AdminPayrollResponse> page =
                new PageImpl<>(List.of(mockAdminPayrollResponse()), PageRequest.of(0, 20), 1);

        when(payrollService.getAllPayrolls(any(AuthenticatedUser.class), any(PayrollFilter.class), any()))
                .thenReturn(page);

        UUID targetUserId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/payrolls")
                        .requestAttr("userId", UUID.randomUUID().toString())
                        .requestAttr("userRole", "ADMIN")
                        .param("userId", targetUserId.toString())
                        .param("status", "PENDING")
                        .param("userRole", "BURUH")
                        .param("referenceType", "HARVEST")
                        .param("dateFrom", "2026-05-01")
                        .param("dateTo", "2026-05-20")
                        .param("sort", "kilogram,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Payrolls retrieved successfully"))
                .andExpect(jsonPath("$.data.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.data.content[0].referenceType").value("HARVEST"));

        ArgumentCaptor<PayrollFilter> filterCaptor = ArgumentCaptor.forClass(PayrollFilter.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        org.mockito.Mockito.verify(payrollService).getAllPayrolls(
                any(AuthenticatedUser.class),
                filterCaptor.capture(),
                pageableCaptor.capture()
        );

        assertEquals(targetUserId, filterCaptor.getValue().userId());
        assertEquals("PENDING", filterCaptor.getValue().status().name());
        assertEquals("BURUH", filterCaptor.getValue().userRole().name());
        assertEquals("HARVEST", filterCaptor.getValue().referenceType().name());
        assertEquals("2026-05-01T00:00Z", filterCaptor.getValue().dateFrom().toString());
        assertEquals("2026-05-21T00:00Z", filterCaptor.getValue().dateTo().toString());
        assertEquals("kilogram: ASC", pageableCaptor.getValue().getSort().toString());
    }

    @Test
    void getAllPayrollsFail() throws Exception {
        when(payrollService.getAllPayrolls(any(AuthenticatedUser.class), any(PayrollFilter.class), any()))
                .thenThrow(new ForbiddenException());

        mockMvc.perform(get("/api/v1/payrolls")
                        .requestAttr("userId", UUID.randomUUID().toString())
                        .requestAttr("userRole", "BURUH"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Forbidden"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void getMyPayrollsSuccess() throws Exception {
        UUID userId = UUID.randomUUID();

        Page<PayrollResponse> page =
                new PageImpl<>(List.of(mockPayrollResponse()), PageRequest.of(0, 20), 1);

        when(payrollService.getMyPayrolls(any(AuthenticatedUser.class), any(PayrollFilter.class), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/payrolls/me")
                        .requestAttr("userId", userId.toString())
                        .requestAttr("userRole", "BURUH")
                        .param("status", "ACCEPTED")
                        .param("dateFrom", "2026-05-01")
                        .param("dateTo", "2026-05-20")
                        .param("sort", "amount,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("My payrolls retrieved successfully"))
                .andExpect(jsonPath("$.data.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.data.content[0].referenceType").value("HARVEST"));

        ArgumentCaptor<PayrollFilter> filterCaptor = ArgumentCaptor.forClass(PayrollFilter.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        org.mockito.Mockito.verify(payrollService).getMyPayrolls(
                any(AuthenticatedUser.class),
                filterCaptor.capture(),
                pageableCaptor.capture()
        );

        assertEquals("ACCEPTED", filterCaptor.getValue().status().name());
        assertEquals("2026-05-01T00:00Z", filterCaptor.getValue().dateFrom().toString());
        assertEquals("2026-05-21T00:00Z", filterCaptor.getValue().dateTo().toString());
        assertEquals("amount: ASC", pageableCaptor.getValue().getSort().toString());
    }

    @Test
    void getPayrollByIdSuccess() throws Exception {
        UUID payrollId = UUID.randomUUID();

        when(payrollService.getPayrollById(any(UUID.class), any(AuthenticatedUser.class)))
                .thenReturn(mockPayrollDetailResponse());

        mockMvc.perform(get("/api/v1/payrolls/" + payrollId)
                        .requestAttr("userId", UUID.randomUUID().toString())
                        .requestAttr("userRole", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Payroll detail retrieved successfully"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.referenceType").value("HARVEST"));
    }

    @Test
    void acceptPayrollSuccess() throws Exception {
        UUID payrollId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        when(payrollService.acceptPayroll(any(UUID.class), any(AuthenticatedUser.class)))
                .thenReturn(mockAcceptPayrollResponse());

        mockMvc.perform(put("/api/v1/payrolls/" + payrollId + "/accept")
                        .requestAttr("userId", adminId.toString())
                        .requestAttr("userRole", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Payroll accepted and disbursed successfully"))
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.data.disbursement.adminWallet").exists())
                .andExpect(jsonPath("$.data.disbursement.workerWallet").exists());
    }

    @Test
    void rejectPayrollSuccess() throws Exception {
        UUID payrollId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        RejectPayrollRequest request = new RejectPayrollRequest();
        request.setRejectionReason("Invalid data");

        when(payrollService.rejectPayroll(
                any(UUID.class),
                any(AuthenticatedUser.class),
                anyString()
        ))
                .thenReturn(mockRejectPayrollResponse());

        mockMvc.perform(put("/api/v1/payrolls/" + payrollId + "/reject")
                        .requestAttr("userId", adminId.toString())
                        .requestAttr("userRole", "ADMIN")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Payroll rejected"))
                .andExpect(jsonPath("$.data.status").value("REJECTED"))
                .andExpect(jsonPath("$.data.rejectionReason").value("Invalid data"));
    }

    @Test
    void rejectPayrollShouldRejectMissingReason() throws Exception {
        UUID payrollId = UUID.randomUUID();

        mockMvc.perform(put("/api/v1/payrolls/" + payrollId + "/reject")
                        .requestAttr("userId", UUID.randomUUID().toString())
                        .requestAttr("userRole", "ADMIN")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(payrollService, never()).rejectPayroll(any(UUID.class), any(AuthenticatedUser.class), any());
    }

    @Test
    void rejectPayrollShouldRejectBlankReason() throws Exception {
        UUID payrollId = UUID.randomUUID();

        mockMvc.perform(put("/api/v1/payrolls/" + payrollId + "/reject")
                        .requestAttr("userId", UUID.randomUUID().toString())
                        .requestAttr("userRole", "ADMIN")
                        .contentType("application/json")
                        .content("{\"rejectionReason\":\"   \"}"))
                .andExpect(status().isBadRequest());

        verify(payrollService, never()).rejectPayroll(any(UUID.class), any(AuthenticatedUser.class), any());
    }

    @Test
    void rejectPayrollShouldRejectTooShortReason() throws Exception {
        UUID payrollId = UUID.randomUUID();

        mockMvc.perform(put("/api/v1/payrolls/" + payrollId + "/reject")
                        .requestAttr("userId", UUID.randomUUID().toString())
                        .requestAttr("userRole", "ADMIN")
                        .contentType("application/json")
                        .content("{\"rejectionReason\":\"short\"}"))
                .andExpect(status().isBadRequest());

        verify(payrollService, never()).rejectPayroll(any(UUID.class), any(AuthenticatedUser.class), any());
    }
}
