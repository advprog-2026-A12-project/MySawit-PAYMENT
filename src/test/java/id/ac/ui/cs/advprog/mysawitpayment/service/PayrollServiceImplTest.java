package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.dto.request.internal.PayrollCreationRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.request.filter.PayrollFilter;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.AcceptPayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.AdminPayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollDetailResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.RejectPayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.internal.PayrollCreationResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.result.WalletMutationResult;
import id.ac.ui.cs.advprog.mysawitpayment.exception.ForbiddenException;
import id.ac.ui.cs.advprog.mysawitpayment.exception.InvalidPayrollRequestException;
import id.ac.ui.cs.advprog.mysawitpayment.exception.PayrollAlreadyProcessedException;
import id.ac.ui.cs.advprog.mysawitpayment.exception.PayrollNotFoundException;
import id.ac.ui.cs.advprog.mysawitpayment.model.Payroll;
import id.ac.ui.cs.advprog.mysawitpayment.model.WageConfig;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.PayrollStatus;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.ReferenceType;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.UserRole;
import id.ac.ui.cs.advprog.mysawitpayment.repository.PayrollRepository;
import id.ac.ui.cs.advprog.mysawitpayment.security.AuthenticatedUser;
import id.ac.ui.cs.advprog.mysawitpayment.security.PaymentAuthorizationService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
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
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class PayrollServiceImplTest {

    @Mock
    private PayrollRepository payrollRepository;

    @Mock
    private WalletService walletService;

    @Mock
    private WageConfigService wageConfigService;

    @Mock
    private PaymentAuthorizationService authorizationService;

    @InjectMocks
    private PayrollServiceImpl payrollService;

    private AuthenticatedUser adminUser(UUID adminId) {
        return new AuthenticatedUser(adminId, UserRole.ADMIN);
    }

    private AuthenticatedUser payrollUser(UUID userId) {
        return new AuthenticatedUser(userId, UserRole.BURUH);
    }

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

    private WageConfig createWageConfig() {
        WageConfig wageConfig = mock(WageConfig.class);

        lenient().when(wageConfig.getUpahBuruhPerKg()).thenReturn(new BigDecimal("2.50"));
        lenient().when(wageConfig.getUpahSupirPerKg()).thenReturn(new BigDecimal("1.50"));
        lenient().when(wageConfig.getUpahMandorPerKg()).thenReturn(new BigDecimal("1.00"));

        return wageConfig;
    }

    private PayrollCreationRequest createPayrollCreationRequest(
            UUID userId,
            UserRole userRole,
            ReferenceType referenceType,
            UUID referenceId,
            BigDecimal kilogram
    ) {
        PayrollCreationRequest request = mock(PayrollCreationRequest.class);

        when(request.getUserId()).thenReturn(userId);
        when(request.getUserRole()).thenReturn(userRole);
        when(request.getReferenceType()).thenReturn(referenceType);
        when(request.getReferenceId()).thenReturn(referenceId);
        when(request.getKilogram()).thenReturn(kilogram);

        return request;
    }

    @Test
    void acceptPayrollShouldDebitAdminCreditWorkerAndReturnResponse() {
        UUID payrollId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        Payroll payroll = createPendingPayroll(payrollId, userId);

        WalletMutationResult adminWalletResult = createWalletMutationResult("50000.00", "49437.39");
        WalletMutationResult workerWalletResult = createWalletMutationResult("688.14", "1250.75");

        when(payrollRepository.findByIdForUpdate(payrollId)).thenReturn(Optional.of(payroll));
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

        AcceptPayrollResponse result = payrollService.acceptPayroll(payrollId, adminUser(adminId));

        assertNotNull(result);
        assertEquals(payrollId, result.getId());
        assertEquals(userId, result.getUser().getId());
        assertEquals("BURUH", result.getUser().getRole());
        assertEquals(new BigDecimal("562.61"), result.getAmount());
        assertEquals("ACCEPTED", result.getStatus());
        assertEquals(adminId, result.getApprovedBy().getId());
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

        verify(payrollRepository).findByIdForUpdate(payrollId);
    }

    @Test
    void acceptPayrollShouldThrowPayrollNotFoundException() {
        UUID payrollId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        when(payrollRepository.findByIdForUpdate(payrollId)).thenReturn(Optional.empty());

        assertThrows(PayrollNotFoundException.class, () ->
                payrollService.acceptPayroll(payrollId, adminUser(adminId))
        );

        verify(payrollRepository).findByIdForUpdate(payrollId);
        verify(payrollRepository, never()).save(any(Payroll.class));
        verifyNoInteractions(walletService);
        verifyNoInteractions(wageConfigService);
    }

    @Test
    void acceptPayrollShouldThrowPayrollAlreadyProcessedException() {
        UUID payrollId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        Payroll payroll = createPendingPayroll(payrollId, userId);
        payroll.setStatus(PayrollStatus.ACCEPTED);

        when(payrollRepository.findByIdForUpdate(payrollId)).thenReturn(Optional.of(payroll));

        assertThrows(PayrollAlreadyProcessedException.class, () ->
                payrollService.acceptPayroll(payrollId, adminUser(adminId))
        );

        verify(payrollRepository).findByIdForUpdate(payrollId);
        verify(payrollRepository, never()).save(any(Payroll.class));
        verifyNoInteractions(walletService);
        verifyNoInteractions(wageConfigService);
    }

    @Test
    void payrollProcessingMethodsShouldBeTransactional() throws Exception {
        boolean acceptTransactional = PayrollServiceImpl.class
                .getMethod("acceptPayroll", UUID.class, AuthenticatedUser.class)
                .isAnnotationPresent(jakarta.transaction.Transactional.class);
        boolean rejectTransactional = PayrollServiceImpl.class
                .getMethod("rejectPayroll", UUID.class, AuthenticatedUser.class, String.class)
                .isAnnotationPresent(jakarta.transaction.Transactional.class);

        assertTrue(acceptTransactional);
        assertTrue(rejectTransactional);
    }

    @Test
    void payrollProcessingLookupShouldUsePessimisticWriteLock() throws Exception {
        org.springframework.data.jpa.repository.Lock lock = PayrollRepository.class
                .getMethod("findByIdForUpdate", UUID.class)
                .getAnnotation(org.springframework.data.jpa.repository.Lock.class);

        assertNotNull(lock);
        assertEquals(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE, lock.value());
    }

    @Test
    void rejectPayrollShouldSetRejectedAndReturnResponse() {
        UUID payrollId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        String reason = "Data kilogram tidak sesuai";

        Payroll payroll = createPendingPayroll(payrollId, userId);

        when(payrollRepository.findByIdForUpdate(payrollId)).thenReturn(Optional.of(payroll));
        when(payrollRepository.save(payroll)).thenReturn(payroll);

        RejectPayrollResponse result = payrollService.rejectPayroll(payrollId, adminUser(adminId), reason);

        assertNotNull(result);
        assertEquals(payrollId, result.getId());
        assertEquals(userId, result.getUser().getId());
        assertEquals("BURUH", result.getUser().getRole());
        assertEquals(new BigDecimal("562.61"), result.getAmount());
        assertEquals("REJECTED", result.getStatus());
        assertEquals(reason, result.getRejectionReason());
        assertEquals(adminId, result.getApprovedBy().getId());
        assertNotNull(result.getApprovedAt());

        ArgumentCaptor<Payroll> payrollCaptor = ArgumentCaptor.forClass(Payroll.class);
        verify(payrollRepository).save(payrollCaptor.capture());

        Payroll savedPayroll = payrollCaptor.getValue();
        assertEquals(PayrollStatus.REJECTED, savedPayroll.getStatus());
        assertEquals(adminId, savedPayroll.getApprovedBy());
        assertEquals(reason, savedPayroll.getRejectionReason());
        assertNotNull(savedPayroll.getApprovedAt());

        verify(payrollRepository).findByIdForUpdate(payrollId);
        verifyNoInteractions(walletService);
        verifyNoInteractions(wageConfigService);
    }

    @Test
    void rejectPayrollShouldThrowPayrollNotFoundException() {
        UUID payrollId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        when(payrollRepository.findByIdForUpdate(payrollId)).thenReturn(Optional.empty());

        assertThrows(PayrollNotFoundException.class, () ->
                payrollService.rejectPayroll(payrollId, adminUser(adminId), "Data tidak valid")
        );

        verify(payrollRepository).findByIdForUpdate(payrollId);
        verify(payrollRepository, never()).save(any(Payroll.class));
        verifyNoInteractions(walletService);
        verifyNoInteractions(wageConfigService);
    }

    @Test
    void rejectPayrollShouldThrowPayrollAlreadyProcessedException() {
        UUID payrollId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        Payroll payroll = createPendingPayroll(payrollId, userId);
        payroll.setStatus(PayrollStatus.REJECTED);

        when(payrollRepository.findByIdForUpdate(payrollId)).thenReturn(Optional.of(payroll));

        assertThrows(PayrollAlreadyProcessedException.class, () ->
                payrollService.rejectPayroll(payrollId, adminUser(adminId), "Data tidak valid")
        );

        verify(payrollRepository).findByIdForUpdate(payrollId);
        verify(payrollRepository, never()).save(any(Payroll.class));
        verifyNoInteractions(walletService);
        verifyNoInteractions(wageConfigService);
    }

    @Test
    void getAllPayrollsShouldReturnMappedAdminPayrollPage() {
        UUID payrollId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Payroll payroll = createPendingPayroll(payrollId, userId);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Payroll> payrollPage = new PageImpl<>(List.of(payroll), pageable, 1);

        PayrollFilter filter = new PayrollFilter(
                userId,
                PayrollStatus.PENDING,
                UserRole.BURUH,
                ReferenceType.HARVEST,
                null,
                null
        );
        when(payrollRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(payrollPage);

        Page<AdminPayrollResponse> result =
                payrollService.getAllPayrolls(adminUser(UUID.randomUUID()), filter, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        AdminPayrollResponse response = result.getContent().get(0);
        assertEquals(payrollId, response.getId());
        assertEquals(userId, response.getUser().getId());
        assertEquals("BURUH", response.getUser().getRole());
        assertEquals(new BigDecimal("562.61"), response.getAmount());
        assertEquals(new BigDecimal("250.50"), response.getKilogram());
        assertEquals(new BigDecimal("2.50"), response.getRatePerKg());
        assertEquals(new BigDecimal("0.90"), response.getMultiplier());
        assertEquals("PENDING", response.getStatus());
        assertEquals("HARVEST", response.getReferenceType());
        assertEquals(payroll.getReferenceId(), response.getReferenceId());
        assertEquals("Upah panen", response.getDescription());
        assertEquals(payroll.getCreatedAt(), response.getCreatedAt());

        verify(payrollRepository).findAll(any(Specification.class), eq(pageable));
        verifyNoInteractions(walletService);
        verifyNoInteractions(wageConfigService);
    }

    @Test
    void getMyPayrollsShouldReturnMappedPayrollPage() {
        UUID payrollId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Payroll payroll = createPendingPayroll(payrollId, userId);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Payroll> payrollPage = new PageImpl<>(List.of(payroll), pageable, 1);

        PayrollFilter filter = new PayrollFilter(null, PayrollStatus.PENDING, null, null, null, null);
        when(payrollRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(payrollPage);

        Page<PayrollResponse> result = payrollService.getMyPayrolls(payrollUser(userId), filter, pageable);

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

        verify(payrollRepository).findAll(any(Specification.class), eq(pageable));
        verifyNoInteractions(walletService);
        verifyNoInteractions(wageConfigService);
    }

    @Test
    void getMyPayrollsShouldAllowNullFilter() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);

        when(payrollRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        Page<PayrollResponse> result = payrollService.getMyPayrolls(payrollUser(userId), null, pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(payrollRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void getAllPayrollsShouldBuildSpecificationWithAllFilters() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);
        PayrollFilter filter = new PayrollFilter(
                userId,
                PayrollStatus.ACCEPTED,
                UserRole.MANDOR,
                ReferenceType.DELIVERY,
                OffsetDateTime.of(2026, 5, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 5, 21, 0, 0, 0, 0, ZoneOffset.UTC)
        );

        when(payrollRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        payrollService.getAllPayrolls(adminUser(UUID.randomUUID()), filter, pageable);

        ArgumentCaptor<Specification<Payroll>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(payrollRepository).findAll(captor.capture(), eq(pageable));

        Root<Payroll> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get(any(String.class))).thenReturn(path);
        when(cb.equal(any(Expression.class), any(Object.class))).thenReturn(predicate);
        when(cb.greaterThanOrEqualTo(any(Expression.class), any(OffsetDateTime.class))).thenReturn(predicate);
        when(cb.lessThan(any(Expression.class), any(OffsetDateTime.class))).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        Predicate result = captor.getValue().toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb, times(4)).equal(any(Expression.class), any(Object.class));
        verify(cb).greaterThanOrEqualTo(any(Expression.class), eq(filter.dateFrom()));
        verify(cb).lessThan(any(Expression.class), eq(filter.dateTo()));
        verify(cb).and(any(Predicate[].class));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void getAllPayrollsShouldBuildSpecificationWithoutFilters() {
        Pageable pageable = PageRequest.of(0, 20);

        when(payrollRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        payrollService.getAllPayrolls(adminUser(UUID.randomUUID()), null, pageable);

        ArgumentCaptor<Specification<Payroll>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(payrollRepository).findAll(captor.capture(), eq(pageable));

        Root<Payroll> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Predicate predicate = mock(Predicate.class);

        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        Predicate result = captor.getValue().toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb, never()).equal(any(Expression.class), any(Object.class));
        verify(cb, never()).greaterThanOrEqualTo(any(Expression.class), any(OffsetDateTime.class));
        verify(cb, never()).lessThan(any(Expression.class), any(OffsetDateTime.class));
        verify(cb).and(any(Predicate[].class));
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

        PayrollDetailResponse result = payrollService.getPayrollById(payrollId, adminUser(UUID.randomUUID()));

        assertNotNull(result);
        assertEquals(payrollId, result.getId());
        assertEquals(userId, result.getUser().getId());
        assertEquals("BURUH", result.getUser().getRole());
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
        verifyNoInteractions(wageConfigService);
    }

    @Test
    void getPayrollByIdShouldThrowPayrollNotFoundException() {
        UUID payrollId = UUID.randomUUID();

        when(payrollRepository.findById(payrollId)).thenReturn(Optional.empty());

        assertThrows(PayrollNotFoundException.class, () ->
                payrollService.getPayrollById(payrollId, adminUser(UUID.randomUUID()))
        );

        verify(payrollRepository).findById(payrollId);
        verifyNoInteractions(walletService);
        verifyNoInteractions(wageConfigService);
    }

    @Test
    void acceptPayrollShouldThrowForbiddenWhenRequesterIsNotAdmin() {
        PayrollServiceImpl securedService = new PayrollServiceImpl(
                payrollRepository,
                walletService,
                wageConfigService,
                new PaymentAuthorizationService()
        );

        assertThrows(ForbiddenException.class, () ->
                securedService.acceptPayroll(UUID.randomUUID(), payrollUser(UUID.randomUUID()))
        );

        verifyNoInteractions(payrollRepository);
        verifyNoInteractions(walletService);
        verifyNoInteractions(wageConfigService);
    }

    @Test
    void getMyPayrollsShouldThrowForbiddenWhenRequesterIsAdmin() {
        PayrollServiceImpl securedService = new PayrollServiceImpl(
                payrollRepository,
                walletService,
                wageConfigService,
                new PaymentAuthorizationService()
        );

        assertThrows(ForbiddenException.class, () ->
                securedService.getMyPayrolls(
                        adminUser(UUID.randomUUID()),
                        new PayrollFilter(null, null, null, null, null, null),
                        PageRequest.of(0, 20)
                )
        );

        verifyNoInteractions(payrollRepository);
        verifyNoInteractions(walletService);
        verifyNoInteractions(wageConfigService);
    }

    @Test
    void getPayrollByIdShouldThrowForbiddenWhenRequesterIsNotOwner() {
        UUID payrollId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        Payroll payroll = createPendingPayroll(payrollId, ownerId);

        when(payrollRepository.findById(payrollId)).thenReturn(Optional.of(payroll));

        PayrollServiceImpl securedService = new PayrollServiceImpl(
                payrollRepository,
                walletService,
                wageConfigService,
                new PaymentAuthorizationService()
        );

        assertThrows(ForbiddenException.class, () ->
                securedService.getPayrollById(payrollId, payrollUser(otherUserId))
        );

        verify(payrollRepository).findById(payrollId);
        verifyNoInteractions(walletService);
        verifyNoInteractions(wageConfigService);
    }

    @Test
    void createPayrollShouldReturnExistingPayrollWhenAlreadyProcessed() {
        UUID payrollId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();

        PayrollCreationRequest request = createPayrollCreationRequest(
                userId,
                UserRole.BURUH,
                ReferenceType.HARVEST,
                referenceId,
                new BigDecimal("250.50")
        );

        Payroll existingPayroll = createPendingPayroll(payrollId, userId);
        existingPayroll.setReferenceType(ReferenceType.HARVEST);
        existingPayroll.setReferenceId(referenceId);

        when(payrollRepository.findByReferenceTypeAndReferenceIdAndUserId(
                ReferenceType.HARVEST,
                referenceId,
                userId
        )).thenReturn(Optional.of(existingPayroll));

        PayrollCreationResponse result = payrollService.createPayroll(request);

        assertNotNull(result);
        assertEquals(payrollId, result.getPayrollId());
        assertTrue(result.isAlreadyProcessed());

        verify(payrollRepository).findByReferenceTypeAndReferenceIdAndUserId(
                ReferenceType.HARVEST,
                referenceId,
                userId
        );
        verify(payrollRepository, never()).save(any(Payroll.class));
        verifyNoInteractions(walletService);
        verifyNoInteractions(wageConfigService);
    }

    @Test
    void createPayrollShouldCreateBuruhPayrollWhenNotAlreadyProcessed() {
        UUID payrollId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();

        PayrollCreationRequest request = createPayrollCreationRequest(
                userId,
                UserRole.BURUH,
                ReferenceType.HARVEST,
                referenceId,
                new BigDecimal("250.50")
        );

        WageConfig wageConfig = createWageConfig();

        when(payrollRepository.findByReferenceTypeAndReferenceIdAndUserId(
                ReferenceType.HARVEST,
                referenceId,
                userId
        )).thenReturn(Optional.empty());
        when(wageConfigService.getActiveWageConfig()).thenReturn(wageConfig);
        when(payrollRepository.save(any(Payroll.class))).thenAnswer(invocation -> {
            Payroll payroll = invocation.getArgument(0);
            payroll.setId(payrollId);
            return payroll;
        });

        PayrollCreationResponse result = payrollService.createPayroll(request);

        assertNotNull(result);
        assertEquals(payrollId, result.getPayrollId());
        assertFalse(result.isAlreadyProcessed());

        ArgumentCaptor<Payroll> payrollCaptor = ArgumentCaptor.forClass(Payroll.class);
        verify(payrollRepository).save(payrollCaptor.capture());

        Payroll savedPayroll = payrollCaptor.getValue();
        assertEquals(userId, savedPayroll.getUserId());
        assertEquals(UserRole.BURUH, savedPayroll.getUserRole());
        assertEquals(ReferenceType.HARVEST, savedPayroll.getReferenceType());
        assertEquals(referenceId, savedPayroll.getReferenceId());
        assertEquals(new BigDecimal("250.50"), savedPayroll.getKilogram());
        assertEquals(new BigDecimal("2.50"), savedPayroll.getRatePerKg());
        assertEquals(new BigDecimal("0.9"), savedPayroll.getMultiplier());
        assertEquals(new BigDecimal("563.63"), savedPayroll.getAmount());
        assertEquals(PayrollStatus.PENDING, savedPayroll.getStatus());
        assertEquals(
                "Upah panen: 250.50 kg × 2.50 SD/kg × 90% = 563.63 SD",
                savedPayroll.getDescription()
        );

        verifyNoInteractions(walletService);
    }

    @Test
    void createPayrollShouldCreateSupirPayrollWhenNotAlreadyProcessed() {
        UUID payrollId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();

        PayrollCreationRequest request = createPayrollCreationRequest(
                userId,
                UserRole.SUPIR_TRUK,
                ReferenceType.DELIVERY,
                referenceId,
                new BigDecimal("370.50")
        );

        WageConfig wageConfig = createWageConfig();

        when(payrollRepository.findByReferenceTypeAndReferenceIdAndUserId(
                ReferenceType.DELIVERY,
                referenceId,
                userId
        )).thenReturn(Optional.empty());
        when(wageConfigService.getActiveWageConfig()).thenReturn(wageConfig);
        when(payrollRepository.save(any(Payroll.class))).thenAnswer(invocation -> {
            Payroll payroll = invocation.getArgument(0);
            payroll.setId(payrollId);
            return payroll;
        });

        PayrollCreationResponse result = payrollService.createPayroll(request);

        assertNotNull(result);
        assertEquals(payrollId, result.getPayrollId());
        assertFalse(result.isAlreadyProcessed());

        ArgumentCaptor<Payroll> payrollCaptor = ArgumentCaptor.forClass(Payroll.class);
        verify(payrollRepository).save(payrollCaptor.capture());

        Payroll savedPayroll = payrollCaptor.getValue();
        assertEquals(userId, savedPayroll.getUserId());
        assertEquals(UserRole.SUPIR_TRUK, savedPayroll.getUserRole());
        assertEquals(ReferenceType.DELIVERY, savedPayroll.getReferenceType());
        assertEquals(referenceId, savedPayroll.getReferenceId());
        assertEquals(new BigDecimal("370.50"), savedPayroll.getKilogram());
        assertEquals(new BigDecimal("1.50"), savedPayroll.getRatePerKg());
        assertEquals(new BigDecimal("500.18"), savedPayroll.getAmount());
        assertEquals(PayrollStatus.PENDING, savedPayroll.getStatus());
        assertEquals(
                "Upah pengiriman: 370.50 kg × 1.50 SD/kg × 90% = 500.18 SD",
                savedPayroll.getDescription()
        );

        verifyNoInteractions(walletService);
    }

    @Test
    void createPayrollShouldCreateMandorPayrollWhenNotAlreadyProcessed() {
        UUID payrollId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();

        PayrollCreationRequest request = createPayrollCreationRequest(
                userId,
                UserRole.MANDOR,
                ReferenceType.DELIVERY,
                referenceId,
                new BigDecimal("300.00")
        );

        WageConfig wageConfig = createWageConfig();

        when(payrollRepository.findByReferenceTypeAndReferenceIdAndUserId(
                ReferenceType.DELIVERY,
                referenceId,
                userId
        )).thenReturn(Optional.empty());
        when(wageConfigService.getActiveWageConfig()).thenReturn(wageConfig);
        when(payrollRepository.save(any(Payroll.class))).thenAnswer(invocation -> {
            Payroll payroll = invocation.getArgument(0);
            payroll.setId(payrollId);
            return payroll;
        });

        PayrollCreationResponse result = payrollService.createPayroll(request);

        assertNotNull(result);
        assertEquals(payrollId, result.getPayrollId());
        assertFalse(result.isAlreadyProcessed());

        ArgumentCaptor<Payroll> payrollCaptor = ArgumentCaptor.forClass(Payroll.class);
        verify(payrollRepository).save(payrollCaptor.capture());

        Payroll savedPayroll = payrollCaptor.getValue();
        assertEquals(userId, savedPayroll.getUserId());
        assertEquals(UserRole.MANDOR, savedPayroll.getUserRole());
        assertEquals(ReferenceType.DELIVERY, savedPayroll.getReferenceType());
        assertEquals(referenceId, savedPayroll.getReferenceId());
        assertEquals(new BigDecimal("300.00"), savedPayroll.getKilogram());
        assertEquals(new BigDecimal("1.00"), savedPayroll.getRatePerKg());
        assertEquals(new BigDecimal("270.00"), savedPayroll.getAmount());
        assertEquals(PayrollStatus.PENDING, savedPayroll.getStatus());
        assertEquals(
                "Upah mandor: 300.00 kg × 1.00 SD/kg × 90% = 270.00 SD",
                savedPayroll.getDescription()
        );

        verifyNoInteractions(walletService);
    }

    @Test
    void createPayrollShouldThrowInvalidPayrollRequestExceptionWhenUserRoleIsAdmin() {
        PayrollCreationRequest request = createPayrollCreationRequest(
                UUID.randomUUID(),
                UserRole.ADMIN,
                ReferenceType.HARVEST,
                UUID.randomUUID(),
                new BigDecimal("100.00")
        );

        assertThrows(InvalidPayrollRequestException.class, () ->
                payrollService.createPayroll(request)
        );

        verifyNoInteractions(payrollRepository);
        verifyNoInteractions(walletService);
        verifyNoInteractions(wageConfigService);
    }

    @Test
    void createPayrollShouldThrowInvalidPayrollRequestExceptionWhenBuruhUsesDeliveryReference() {
        PayrollCreationRequest request = createPayrollCreationRequest(
                UUID.randomUUID(),
                UserRole.BURUH,
                ReferenceType.DELIVERY,
                UUID.randomUUID(),
                new BigDecimal("100.00")
        );

        assertThrows(InvalidPayrollRequestException.class, () ->
                payrollService.createPayroll(request)
        );

        verifyNoInteractions(payrollRepository);
        verifyNoInteractions(walletService);
        verifyNoInteractions(wageConfigService);
    }

    @Test
    void createPayrollShouldThrowInvalidPayrollRequestExceptionWhenSupirUsesHarvestReference() {
        PayrollCreationRequest request = createPayrollCreationRequest(
                UUID.randomUUID(),
                UserRole.SUPIR_TRUK,
                ReferenceType.HARVEST,
                UUID.randomUUID(),
                new BigDecimal("100.00")
        );

        assertThrows(InvalidPayrollRequestException.class, () ->
                payrollService.createPayroll(request)
        );

        verifyNoInteractions(payrollRepository);
        verifyNoInteractions(walletService);
        verifyNoInteractions(wageConfigService);
    }

    @Test
    void createPayrollShouldThrowInvalidPayrollRequestExceptionWhenMandorUsesHarvestReference() {
        PayrollCreationRequest request = createPayrollCreationRequest(
                UUID.randomUUID(),
                UserRole.MANDOR,
                ReferenceType.HARVEST,
                UUID.randomUUID(),
                new BigDecimal("100.00")
        );

        assertThrows(InvalidPayrollRequestException.class, () ->
                payrollService.createPayroll(request)
        );

        verifyNoInteractions(payrollRepository);
        verifyNoInteractions(walletService);
        verifyNoInteractions(wageConfigService);
    }
}
