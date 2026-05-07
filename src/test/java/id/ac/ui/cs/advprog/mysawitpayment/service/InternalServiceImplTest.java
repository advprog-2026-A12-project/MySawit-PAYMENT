package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.dto.request.internal.PayrollCreationRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.request.internal.WalletCreationRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.internal.PayrollCreationResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.internal.WalletCreationResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalServiceImplTest {

    @Mock
    private WalletService walletService;

    @Mock
    private PayrollService payrollService;

    @InjectMocks
    private InternalServiceImpl internalService;

    @Test
    void createWalletShouldDelegateToWalletService() {
        UUID walletId = UUID.randomUUID();

        WalletCreationRequest request = mock(WalletCreationRequest.class);
        WalletCreationResponse expectedResponse = WalletCreationResponse.builder()
                .walletId(walletId)
                .alreadyProcessed(false)
                .build();

        when(walletService.createWallet(request)).thenReturn(expectedResponse);

        WalletCreationResponse result = internalService.createWallet(request);

        assertNotNull(result);
        assertEquals(walletId, result.getWalletId());
        assertEquals(expectedResponse.isAlreadyProcessed(), result.isAlreadyProcessed());

        verify(walletService).createWallet(request);
    }

    @Test
    void createPayrollShouldDelegateToPayrollService() {
        UUID payrollId = UUID.randomUUID();

        PayrollCreationRequest request = mock(PayrollCreationRequest.class);
        PayrollCreationResponse expectedResponse = PayrollCreationResponse.builder()
                .payrollId(payrollId)
                .alreadyProcessed(false)
                .build();

        when(payrollService.createPayroll(request)).thenReturn(expectedResponse);

        PayrollCreationResponse result = internalService.createPayroll(request);

        assertNotNull(result);
        assertEquals(payrollId, result.getPayrollId());
        assertEquals(expectedResponse.isAlreadyProcessed(), result.isAlreadyProcessed());

        verify(payrollService).createPayroll(request);
    }
}