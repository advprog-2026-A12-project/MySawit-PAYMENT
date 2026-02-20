package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.dto.request.CreatePayrollRequest;
import id.ac.ui.cs.advprog.mysawitpayment.model.Payroll;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.PayrollStatus;
import id.ac.ui.cs.advprog.mysawitpayment.repository.PayrollRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class PayrollServiceTest {

    @Mock
    private PayrollRepository payrollRepository;

    @InjectMocks
    private PayrollServiceImpl payrollService;

    private Payroll payroll;

    @BeforeEach
    void setUp() {
        payroll = new Payroll();
        payroll.setId(1L);
        payroll.setUserId(10L);
        payroll.setKilogram(100.0);
        payroll.setAmount(100.0);
        payroll.setStatus(PayrollStatus.PENDING);
        payroll.setReferenceId("123");
        payroll.setReferenceType("HARVEST");
        payroll.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createPayrollSuccess() {
        CreatePayrollRequest request = new CreatePayrollRequest();
        request.setUserId(10L);
        request.setKilogram(100.0);
        request.setReferenceId("123");
        request.setReferenceType("HARVEST");

        when(payrollRepository.save(any())).thenReturn(payroll);

        var response = payrollService.createPayroll(request);

        assertNotNull(response);
        assertEquals(PayrollStatus.PENDING, response.getStatus());
        verify(payrollRepository, times(1)).save(any());
    }

    @Test
    void getPayrollByUserSuccess() {
        when(payrollRepository.findByUserId(10L)).thenReturn(List.of(payroll));

        var result = payrollService.getPayrollByUser(10L);

        assertEquals(1, result.size());
        verify(payrollRepository).findByUserId(10L);
    }

    @Test
    void approvePayrollSuccess() {
        when(payrollRepository.findById(1L)).thenReturn(Optional.of(payroll));

        payrollService.approvePayroll(1L);

        assertEquals(PayrollStatus.ACCEPTED, payroll.getStatus());
        verify(payrollRepository).save(payroll);
    }

    @Test
    void approvePayrollNotFound() {
        when(payrollRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> payrollService.approvePayroll(1L));
    }

    @Test
    void rejectPayrollSuccess() {
        when(payrollRepository.findById(1L)).thenReturn(Optional.of(payroll));

        payrollService.rejectPayroll(1L, "invalid");

        assertEquals(PayrollStatus.REJECTED, payroll.getStatus());
        verify(payrollRepository).save(payroll);
    }

    @Test
    void rejectPayrollNotFound() {
        when(payrollRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> payrollService.rejectPayroll(1L, "invalid"));
    }
}