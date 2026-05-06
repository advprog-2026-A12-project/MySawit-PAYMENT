package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.dto.response.AdminPayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollDetailResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.ReferenceType;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.UserRole;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.AcceptPayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.RejectPayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.result.WalletMutationResult;
import id.ac.ui.cs.advprog.mysawitpayment.exception.PayrollAlreadyProcessedException;
import id.ac.ui.cs.advprog.mysawitpayment.exception.PayrollNotFoundException;
import id.ac.ui.cs.advprog.mysawitpayment.model.Payroll;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.PayrollStatus;
import id.ac.ui.cs.advprog.mysawitpayment.repository.PayrollRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayrollServiceImplTest {

    @Mock
    private PayrollRepository payrollRepository;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private PayrollServiceImpl payrollService;

    private Payroll createPendingPayroll(UUID payrollId, UUID userId) {
        return Payroll.builder()
                .id(payrollId)
                .userId(userId)
                .userRole(UserRole.BURUH)
                .amount(new BigDecimal("562.61"))
                .kilogram(new BigDecimal("250.50"))
                .ratePerKg(new BigDecimal("2.50"))
                .multiplier(new BigDecimal("0.90"))
                .status(PayrollStatus.PENDING)
                .description("Upah panen")
                .referenceType(ReferenceType.HARVEST)
                .referenceId(UUID.randomUUID())
                .createdAt(OffsetDateTime.of(2026, 4, 16, 10, 0, 0, 0, ZoneOffset.UTC))
                .updatedAt(OffsetDateTime.of(2026, 4, 16, 11, 0, 0, 0, ZoneOffset.UTC))
                .build();
    }

    private WalletMutationResult createWalletMutationResult(String balanceBefore, String balanceAfter) {
        return WalletMutationResult.builder()
                .balanceBefore(new BigDecimal(balanceBefore))
                .balanceAfter(new BigDecimal(balanceAfter))
                .build();
    }

    @Test
    void acceptPayrollShouldDebitAdminCreditWorkerAndReturnResponse() {
        UUID payrollId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        Payroll payroll = createPendingPayroll(payrollId, userId);

        WalletMutationResult adminWalletResult = createWalletMutationResult("50000.00", "49437.39");
        WalletMutationResult workerWalletResult = createWalletMutationResult("688.14", "1250.75");

        when(payrollRepository.findById(payrollId)).thenReturn(Optional.of(payroll));
        when(walletService.debitWallet(
                adminId,
                payroll.getAmount(),
                "PAYROLL_DEDUCTION",
                payrollId,
                "Pengurangan saldo untuk pembayaran payroll"
        )).thenReturn(adminWalletResult);
        when(walletService.creditWallet(
                userId,
                payroll.getAmount(),
                "PAYROLL_DISBURSEMENT",
                payrollId,
                "Pencairan payroll"
        )).thenReturn(workerWalletResult);
        when(payrollRepository.save(payroll)).thenReturn(payroll);

        AcceptPayrollResponse result = payrollService.acceptPayroll(payrollId, adminId);

        assertNotNull(result);
        assertEquals(payrollId, result.getId());
        assertEquals(userId, result.getUser().getId());
        assertEquals(new BigDecimal("562.61"), result.getAmount());
        assertEquals("ACCEPTED", result.getStatus());
        assertEquals(adminId, result.getApprovedBy().getId());
        assertEquals("", result.getApprovedBy().getName());
        assertNotNull(result.getApprovedAt());

        assertEquals(new BigDecimal("50000.00"), result.getDisbursement().getAdminWallet().getBalanceBefore());
        assertEquals(new BigDecimal("49437.39"), result.getDisbursement().getAdminWallet().getBalanceAfter());
        assertEquals(new BigDecimal("688.14"), result.getDisbursement().getWorkerWallet().getBalanceBefore());
        assertEquals(new BigDecimal("1250.75"), result.getDisbursement().getWorkerWallet().getBalanceAfter());

        ArgumentCaptor<Payroll> payrollCaptor = ArgumentCaptor.forClass(Payroll.class);
        verify(payrollRepository).save(payrollCaptor.capture());

        Payroll savedPayroll = payrollCaptor.getValue();
        assertEquals(PayrollStatus.ACCEPTED, savedPayroll.getStatus());
        assertEquals(adminId, savedPayroll.getApprovedBy());
        assertNotNull(savedPayroll.getApprovedAt());

        verify(payrollRepository).findById(payrollId);
        verify(walletService).debitWallet(
                adminId,
                payroll.getAmount(),
                "PAYROLL_DEDUCTION",
                payrollId,
                "Pengurangan saldo untuk pembayaran payroll"
        );
        verify(walletService).creditWallet(
                userId,
                payroll.getAmount(),
                "PAYROLL_DISBURSEMENT",
                payrollId,
                "Pencairan payroll"
        );
    }

    @Test
    void acceptPayrollShouldThrowPayrollNotFoundException() {
        UUID payrollId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        when(payrollRepository.findById(payrollId)).thenReturn(Optional.empty());

        assertThrows(PayrollNotFoundException.class, () ->
                payrollService.acceptPayroll(payrollId, adminId)
        );

        verify(payrollRepository).findById(payrollId);
        verify(payrollRepository, never()).save(any(Payroll.class));
        verifyNoInteractions(walletService);
    }

    @Test
    void acceptPayrollShouldThrowPayrollAlreadyProcessedException() {
        UUID payrollId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        Payroll payroll = createPendingPayroll(payrollId, userId);
        payroll.setStatus(PayrollStatus.ACCEPTED);

        when(payrollRepository.findById(payrollId)).thenReturn(Optional.of(payroll));

        assertThrows(PayrollAlreadyProcessedException.class, () ->
                payrollService.acceptPayroll(payrollId, adminId)
        );

        verify(payrollRepository).findById(payrollId);
        verify(payrollRepository, never()).save(any(Payroll.class));
        verifyNoInteractions(walletService);
    }

    @Test
    void rejectPayrollShouldSetRejectedAndReturnResponse() {
        UUID payrollId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        String reason = "Data kilogram tidak sesuai";

        Payroll payroll = createPendingPayroll(payrollId, userId);

        when(payrollRepository.findById(payrollId)).thenReturn(Optional.of(payroll));
        when(payrollRepository.save(payroll)).thenReturn(payroll);

        RejectPayrollResponse result = payrollService.rejectPayroll(payrollId, adminId, reason);

        assertNotNull(result);
        assertEquals(payrollId, result.getId());
        assertEquals(userId, result.getUser().getId());
        assertEquals(new BigDecimal("562.61"), result.getAmount());
        assertEquals("REJECTED", result.getStatus());
        assertEquals(reason, result.getRejectionReason());
        assertEquals(adminId, result.getApprovedBy().getId());
        assertEquals("", result.getApprovedBy().getName());
        assertNotNull(result.getApprovedAt());

        ArgumentCaptor<Payroll> payrollCaptor = ArgumentCaptor.forClass(Payroll.class);
        verify(payrollRepository).save(payrollCaptor.capture());

        Payroll savedPayroll = payrollCaptor.getValue();
        assertEquals(PayrollStatus.REJECTED, savedPayroll.getStatus());
        assertEquals(adminId, savedPayroll.getApprovedBy());
        assertEquals(reason, savedPayroll.getRejectionReason());
        assertNotNull(savedPayroll.getApprovedAt());

        verify(payrollRepository).findById(payrollId);
        verifyNoInteractions(walletService);
    }

    @Test
    void rejectPayrollShouldThrowPayrollNotFoundException() {
        UUID payrollId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        when(payrollRepository.findById(payrollId)).thenReturn(Optional.empty());

        assertThrows(PayrollNotFoundException.class, () ->
                payrollService.rejectPayroll(payrollId, adminId, "Data tidak valid")
        );

        verify(payrollRepository).findById(payrollId);
        verify(payrollRepository, never()).save(any(Payroll.class));
        verifyNoInteractions(walletService);
    }

    @Test
    void rejectPayrollShouldThrowPayrollAlreadyProcessedException() {
        UUID payrollId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        Payroll payroll = createPendingPayroll(payrollId, userId);
        payroll.setStatus(PayrollStatus.REJECTED);

        when(payrollRepository.findById(payrollId)).thenReturn(Optional.of(payroll));

        assertThrows(PayrollAlreadyProcessedException.class, () ->
                payrollService.rejectPayroll(payrollId, adminId, "Data tidak valid")
        );

        verify(payrollRepository).findById(payrollId);
        verify(payrollRepository, never()).save(any(Payroll.class));
        verifyNoInteractions(walletService);
    }

    @Test
    void getAllPayrollsShouldReturnMappedAdminPayrollPage() {
        UUID payrollId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Payroll payroll = createPendingPayroll(payrollId, userId);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Payroll> payrollPage = new PageImpl<>(List.of(payroll), pageable, 1);

        when(payrollRepository.findAll(pageable)).thenReturn(payrollPage);

        Page<AdminPayrollResponse> result = payrollService.getAllPayrolls(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        AdminPayrollResponse response = result.getContent().get(0);
        assertEquals(payrollId, response.getId());
        assertEquals(userId, response.getUser().getId());
        assertEquals(new BigDecimal("562.61"), response.getAmount());
        assertEquals(new BigDecimal("250.50"), response.getKilogram());
        assertEquals(new BigDecimal("2.50"), response.getRatePerKg());
        assertEquals(new BigDecimal("0.90"), response.getMultiplier());
        assertEquals("PENDING", response.getStatus());
        assertEquals("HARVEST", response.getReferenceType());
        assertEquals(payroll.getReferenceId(), response.getReferenceId());
        assertEquals("Upah panen", response.getDescription());
        assertEquals(payroll.getCreatedAt(), response.getCreatedAt());

        verify(payrollRepository).findAll(pageable);
        verifyNoInteractions(walletService);
    }

    @Test
    void getMyPayrollsShouldReturnMappedPayrollPage() {
        UUID payrollId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Payroll payroll = createPendingPayroll(payrollId, userId);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Payroll> payrollPage = new PageImpl<>(List.of(payroll), pageable, 1);

        when(payrollRepository.findByUserId(userId, pageable)).thenReturn(payrollPage);

        Page<PayrollResponse> result = payrollService.getMyPayrolls(userId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        PayrollResponse response = result.getContent().get(0);
        assertEquals(payrollId, response.getId());
        assertEquals(new BigDecimal("562.61"), response.getAmount());
        assertEquals(new BigDecimal("250.50"), response.getKilogram());
        assertEquals(new BigDecimal("2.50"), response.getRatePerKg());
        assertEquals(new BigDecimal("0.90"), response.getMultiplier());
        assertEquals("PENDING", response.getStatus());
        assertEquals("HARVEST", response.getReferenceType());
        assertEquals("Upah panen", response.getDescription());
        assertEquals(payroll.getApprovedAt(), response.getApprovedAt());
        assertEquals(payroll.getCreatedAt(), response.getCreatedAt());

        verify(payrollRepository).findByUserId(userId, pageable);
        verifyNoInteractions(walletService);
    }

    @Test
    void getPayrollByIdShouldReturnMappedDetailResponse() {
        UUID payrollId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        Payroll payroll = createPendingPayroll(payrollId, userId);
        payroll.setStatus(PayrollStatus.REJECTED);
        payroll.setApprovedBy(adminId);
        payroll.setApprovedAt(OffsetDateTime.of(2026, 4, 16, 12, 0, 0, 0, ZoneOffset.UTC));
        payroll.setRejectionReason("Data kilogram tidak sesuai");

        when(payrollRepository.findById(payrollId)).thenReturn(Optional.of(payroll));

        PayrollDetailResponse result = payrollService.getPayrollById(payrollId);

        assertNotNull(result);
        assertEquals(payrollId, result.getId());
        assertEquals(userId, result.getUser().getId());
        assertEquals(new BigDecimal("562.61"), result.getAmount());
        assertEquals(new BigDecimal("250.50"), result.getKilogram());
        assertEquals(new BigDecimal("2.50"), result.getRatePerKg());
        assertEquals(new BigDecimal("0.90"), result.getMultiplier());
        assertEquals("REJECTED", result.getStatus());
        assertEquals("Upah panen", result.getDescription());
        assertEquals("Data kilogram tidak sesuai", result.getRejectionReason());
        assertEquals("HARVEST", result.getReferenceType());
        assertEquals(payroll.getReferenceId(), result.getReferenceId());
        assertEquals(adminId, result.getApprovedBy().getId());
        assertEquals(payroll.getApprovedAt(), result.getApprovedAt());
        assertEquals(payroll.getCreatedAt(), result.getCreatedAt());
        assertEquals(payroll.getUpdatedAt(), result.getUpdatedAt());

        verify(payrollRepository).findById(payrollId);
        verifyNoInteractions(walletService);
    }

    @Test
    void getPayrollByIdShouldThrowPayrollNotFoundException() {
        UUID payrollId = UUID.randomUUID();

        when(payrollRepository.findById(payrollId)).thenReturn(Optional.empty());

        assertThrows(PayrollNotFoundException.class, () ->
                payrollService.getPayrollById(payrollId)
        );

        verify(payrollRepository).findById(payrollId);
        verifyNoInteractions(walletService);
    }

    @Test
    void createPayrollShouldSavePayrollAndReturnResponse() {
        UUID payrollId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Payroll payroll = createPendingPayroll(payrollId, userId);

        when(payrollRepository.save(payroll)).thenReturn(payroll);

        PayrollResponse result = payrollService.createPayroll(payroll);

        assertNotNull(result);
        assertEquals(payrollId, result.getId());
        assertEquals(new BigDecimal("562.61"), result.getAmount());
        assertEquals(new BigDecimal("250.50"), result.getKilogram());
        assertEquals(new BigDecimal("2.50"), result.getRatePerKg());
        assertEquals(new BigDecimal("0.90"), result.getMultiplier());
        assertEquals("PENDING", result.getStatus());
        assertEquals("HARVEST", result.getReferenceType());
        assertEquals("Upah panen", result.getDescription());
        assertEquals(payroll.getApprovedAt(), result.getApprovedAt());
        assertEquals(payroll.getCreatedAt(), result.getCreatedAt());

        verify(payrollRepository).save(payroll);
        verifyNoInteractions(walletService);
    }
}