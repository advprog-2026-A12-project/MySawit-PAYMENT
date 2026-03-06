package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.exception.PayrollAlreadyProcessedException;
import id.ac.ui.cs.advprog.mysawitpayment.exception.PayrollNotFoundException;
import id.ac.ui.cs.advprog.mysawitpayment.model.Payroll;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.PayrollStatus;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.ReferenceType;
import id.ac.ui.cs.advprog.mysawitpayment.repository.PayrollRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PayrollServiceImplTest {

    @Mock
    private PayrollRepository payrollRepository;

    @InjectMocks
    private PayrollServiceImpl payrollService;

    private Payroll payroll;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        payroll = Payroll.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .amount(new BigDecimal("1000"))
                .kilogram(new BigDecimal("10"))
                .ratePerKg(new BigDecimal("100"))
                .multiplier(new BigDecimal("0.9"))
                .status(PayrollStatus.PENDING)
                .referenceType(ReferenceType.HARVEST)
                .description("Test payroll")
                .createdAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void testGetAllPayrolls() {

        Page<Payroll> page = new PageImpl<>(List.of(payroll));

        when(payrollRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<PayrollResponse> result =
                payrollService.getAllPayrolls(PageRequest.of(0,10));

        assertEquals(1, result.getContent().size());
    }

    @Test
    void testGetMyPayrolls() {

        Page<Payroll> page = new PageImpl<>(List.of(payroll));

        when(payrollRepository.findByUserId(eq(payroll.getUserId()), any()))
                .thenReturn(page);

        Page<PayrollResponse> result =
                payrollService.getMyPayrolls(payroll.getUserId(), PageRequest.of(0,10));

        assertEquals(1, result.getContent().size());
    }

    @Test
    void testGetPayrollById() {

        when(payrollRepository.findById(payroll.getId()))
                .thenReturn(Optional.of(payroll));

        PayrollResponse response =
                payrollService.getPayrollById(payroll.getId());

        assertEquals(payroll.getId(), response.getId());
    }

    @Test
    void testGetPayrollByIdNotFound() {

        when(payrollRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(
                PayrollNotFoundException.class,
                () -> payrollService.getPayrollById(UUID.randomUUID())
        );
    }

    @Test
    void testApprovePayroll() {

        UUID adminId = UUID.randomUUID();

        when(payrollRepository.findById(payroll.getId()))
                .thenReturn(Optional.of(payroll));

        when(payrollRepository.save(any()))
                .thenReturn(payroll);

        PayrollResponse response =
                payrollService.approvePayroll(payroll.getId(), adminId);

        assertEquals("ACCEPTED", response.getStatus());
    }

    @Test
    void testRejectPayroll() {

        UUID adminId = UUID.randomUUID();

        when(payrollRepository.findById(payroll.getId()))
                .thenReturn(Optional.of(payroll));

        when(payrollRepository.save(any()))
                .thenReturn(payroll);

        PayrollResponse response =
                payrollService.rejectPayroll(payroll.getId(), adminId, "Invalid");

        assertEquals("REJECTED", response.getStatus());
    }

    @Test
    void testApprovePayrollAlreadyProcessed() {

        payroll.setStatus(PayrollStatus.ACCEPTED);

        when(payrollRepository.findById(payroll.getId()))
                .thenReturn(Optional.of(payroll));

        assertThrows(
                PayrollAlreadyProcessedException.class,
                () -> payrollService.approvePayroll(payroll.getId(), UUID.randomUUID())
        );
    }

    @Test
    void testCreatePayroll() {

        when(payrollRepository.save(any()))
                .thenReturn(payroll);

        PayrollResponse response =
                payrollService.createPayroll(payroll);

        assertEquals(payroll.getAmount(), response.getAmount());
    }
}