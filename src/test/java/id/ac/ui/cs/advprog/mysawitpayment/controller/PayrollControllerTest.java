package id.ac.ui.cs.advprog.mysawitpayment.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import id.ac.ui.cs.advprog.mysawitpayment.dto.request.CreatePayrollRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.PayrollStatus;
import id.ac.ui.cs.advprog.mysawitpayment.service.PayrollService;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class PayrollControllerTest {

    @Mock
    private PayrollService payrollService;

    @InjectMocks
    private PayrollController payrollController;

    private CreatePayrollRequest request;
    private PayrollResponse response;

    @BeforeEach
    void setUp() {
        request = new CreatePayrollRequest();
        request.setUserId(10L);
        request.setKilogram(100.0);
        request.setReferenceId("123");
        request.setReferenceType("HARVEST");

        response = new PayrollResponse();
        response.setId(1L);
        response.setUserId(10L);
        response.setKilogram(100.0);
        response.setAmount(100.0);
        response.setStatus(PayrollStatus.PENDING);
        response.setReferenceId("123");
        response.setReferenceType("HARVEST");
        response.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createPayrollReturnsOk() {
        when(payrollService.createPayroll(request)).thenReturn(response);

        ResponseEntity<PayrollResponse> result =
                payrollController.create(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1L, result.getBody().getId());

        verify(payrollService).createPayroll(request);
    }

    @Test
    void getPayrollByUserReturnsList() {
        when(payrollService.getPayrollByUser(10L))
                .thenReturn(List.of(response));

        ResponseEntity<List<PayrollResponse>> result =
                payrollController.getByUser(10L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        assertEquals(10L, result.getBody().getFirst().getUserId());

        verify(payrollService).getPayrollByUser(10L);
    }

    @Test
    void approvePayrollReturnsOk() {
        ResponseEntity<Void> result =
                payrollController.approve(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());

        verify(payrollService).approvePayroll(1L);
    }

    @Test
    void rejectPayrollReturnsOk() {
        ResponseEntity<Void> result =
                payrollController.reject(1L, "invalid");

        assertEquals(HttpStatus.OK, result.getStatusCode());

        verify(payrollService).rejectPayroll(1L, "invalid");
    }
}