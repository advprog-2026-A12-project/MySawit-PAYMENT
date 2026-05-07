package id.ac.ui.cs.advprog.mysawitpayment.controller;

import id.ac.ui.cs.advprog.mysawitpayment.dto.response.internal.PayrollCreationResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.internal.WalletCreationResponse;
import id.ac.ui.cs.advprog.mysawitpayment.security.InternalApiKeyFilter;
import id.ac.ui.cs.advprog.mysawitpayment.security.JwtFilter;
import id.ac.ui.cs.advprog.mysawitpayment.service.InternalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = InternalController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = JwtFilter.class
                ),
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = InternalApiKeyFilter.class
                )
        }
)
class InternalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InternalService internalService;

    @Test
    void createPayrollShouldReturnCreatedMessageWhenPayrollIsNew() throws Exception {
        UUID payrollId = UUID.randomUUID();

        PayrollCreationResponse response = PayrollCreationResponse.builder()
                .payrollId(payrollId)
                .alreadyProcessed(false)
                .build();

        when(internalService.createPayroll(any()))
                .thenReturn(response);

        String requestBody = """
                {
                  "userId": "%s",
                  "userRole": "BURUH",
                  "referenceType": "HARVEST",
                  "referenceId": "%s",
                  "kilogram": 250.50
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID());

        mockMvc.perform(post("/api/v1/internal/payrolls")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Payroll created successfully"))
                .andExpect(jsonPath("$.data.payrollId").value(payrollId.toString()))
                .andExpect(jsonPath("$.data.alreadyProcessed").value(false));
    }

    @Test
    void createPayrollShouldReturnAlreadyExistsMessageWhenPayrollAlreadyProcessed() throws Exception {
        UUID payrollId = UUID.randomUUID();

        PayrollCreationResponse response = PayrollCreationResponse.builder()
                .payrollId(payrollId)
                .alreadyProcessed(true)
                .build();

        when(internalService.createPayroll(any()))
                .thenReturn(response);

        String requestBody = """
                {
                  "userId": "%s",
                  "userRole": "BURUH",
                  "referenceType": "HARVEST",
                  "referenceId": "%s",
                  "kilogram": 250.50
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID());

        mockMvc.perform(post("/api/v1/internal/payrolls")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Payroll already exists"))
                .andExpect(jsonPath("$.data.payrollId").value(payrollId.toString()))
                .andExpect(jsonPath("$.data.alreadyProcessed").value(true));
    }

    @Test
    void createWalletShouldReturnCreatedMessageWhenWalletIsNew() throws Exception {
        UUID walletId = UUID.randomUUID();

        WalletCreationResponse response = WalletCreationResponse.builder()
                .walletId(walletId)
                .alreadyProcessed(false)
                .build();

        when(internalService.createWallet(any()))
                .thenReturn(response);

        String requestBody = """
                {
                  "userId": "%s"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/internal/wallets")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Wallet created successfully"))
                .andExpect(jsonPath("$.data.walletId").value(walletId.toString()))
                .andExpect(jsonPath("$.data.alreadyProcessed").value(false));
    }

    @Test
    void createWalletShouldReturnAlreadyExistsMessageWhenWalletAlreadyProcessed() throws Exception {
        UUID walletId = UUID.randomUUID();

        WalletCreationResponse response = WalletCreationResponse.builder()
                .walletId(walletId)
                .alreadyProcessed(true)
                .build();

        when(internalService.createWallet(any()))
                .thenReturn(response);

        String requestBody = """
                {
                  "userId": "%s"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/internal/wallets")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Wallet already exists"))
                .andExpect(jsonPath("$.data.walletId").value(walletId.toString()))
                .andExpect(jsonPath("$.data.alreadyProcessed").value(true));
    }
}