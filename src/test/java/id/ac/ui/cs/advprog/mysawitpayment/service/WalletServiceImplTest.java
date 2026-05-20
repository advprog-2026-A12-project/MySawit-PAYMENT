package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.dto.request.filter.WalletTransactionFilter;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.AdminWalletResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.WalletResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.WalletTransactionResponse;
import id.ac.ui.cs.advprog.mysawitpayment.exception.ForbiddenException;
import id.ac.ui.cs.advprog.mysawitpayment.exception.WalletNotFoundException;
import id.ac.ui.cs.advprog.mysawitpayment.model.Wallet;
import id.ac.ui.cs.advprog.mysawitpayment.model.WalletTransaction;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.TransactionType;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.UserRole;
import id.ac.ui.cs.advprog.mysawitpayment.repository.WalletRepository;
import id.ac.ui.cs.advprog.mysawitpayment.repository.WalletTransactionRepository;
import id.ac.ui.cs.advprog.mysawitpayment.security.AuthenticatedUser;
import id.ac.ui.cs.advprog.mysawitpayment.security.PaymentAuthorizationService;
import id.ac.ui.cs.advprog.mysawitpayment.dto.result.WalletMutationResult;
import id.ac.ui.cs.advprog.mysawitpayment.dto.request.internal.WalletCreationRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.internal.WalletCreationResponse;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @Mock
    private PaymentAuthorizationService authorizationService;

    @InjectMocks
    private WalletServiceImpl walletService;

    private AuthenticatedUser requester(UUID userId, UserRole role) {
        return new AuthenticatedUser(userId, role);
    }

    private Wallet createWallet(UUID walletId, UUID userId) {
        return Wallet.builder()
                .id(walletId)
                .userId(userId)
                .balance(new BigDecimal("1500.00"))
                .createdAt(OffsetDateTime.of(2026, 4, 16, 10, 0, 0, 0, ZoneOffset.UTC))
                .updatedAt(OffsetDateTime.of(2026, 4, 16, 11, 0, 0, 0, ZoneOffset.UTC))
                .build();
    }

    private WalletTransaction createTransaction(UUID transactionId, UUID walletId, UUID referenceId) {
        return WalletTransaction.builder()
                .id(transactionId)
                .walletId(walletId)
                .transactionType(TransactionType.CREDIT)
                .amount(new BigDecimal("500.00"))
                .balanceBefore(new BigDecimal("1000.00"))
                .balanceAfter(new BigDecimal("1500.00"))
                .referenceType("PAYROLL")
                .referenceId(referenceId)
                .description("Payroll disbursement")
                .createdAt(OffsetDateTime.of(2026, 4, 16, 12, 0, 0, 0, ZoneOffset.UTC))
                .build();
    }

    @Test
    void getMyWalletShouldReturnMappedResponse() {
        UUID walletId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Wallet wallet = createWallet(walletId, userId);

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));

        WalletResponse result = walletService.getMyWallet(requester(userId, UserRole.BURUH));

        assertNotNull(result);
        assertEquals(walletId, result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(new BigDecimal("1500.00"), result.getBalance());
        assertEquals("SawitDollar", result.getCurrency());
        assertEquals(wallet.getCreatedAt(), result.getCreatedAt());
        assertEquals(wallet.getUpdatedAt(), result.getUpdatedAt());

        verify(authorizationService).requireOwnWalletAccess(any(AuthenticatedUser.class));
        verify(walletRepository).findByUserId(userId);
        verifyNoInteractions(walletTransactionRepository);
    }

    @Test
    void getMyWalletShouldThrowWalletNotFoundException() {
        UUID userId = UUID.randomUUID();

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(
                WalletNotFoundException.class,
                () -> walletService.getMyWallet(requester(userId, UserRole.BURUH))
        );

        verify(authorizationService).requireOwnWalletAccess(any(AuthenticatedUser.class));
        verify(walletRepository).findByUserId(userId);
        verifyNoInteractions(walletTransactionRepository);
    }

    @Test
    void getMyWalletShouldThrowForbiddenWhenRequesterIsNotAllowed() {
        AuthenticatedUser requester = requester(UUID.randomUUID(), UserRole.BURUH);
        doThrow(new ForbiddenException())
                .when(authorizationService)
                .requireOwnWalletAccess(requester);

        assertThrows(ForbiddenException.class, () -> walletService.getMyWallet(requester));

        verify(authorizationService).requireOwnWalletAccess(requester);
        verifyNoInteractions(walletRepository);
        verifyNoInteractions(walletTransactionRepository);
    }

    @Test
    void getWalletByUserIdShouldReturnMappedResponse() {
        UUID walletId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Wallet wallet = createWallet(walletId, userId);

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));

        AdminWalletResponse result = walletService.getWalletByUserId(
                requester(UUID.randomUUID(), UserRole.ADMIN),
                userId
        );

        assertNotNull(result);
        assertEquals(walletId, result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(new BigDecimal("1500.00"), result.getBalance());
        assertEquals("SawitDollar", result.getCurrency());
        assertEquals(wallet.getCreatedAt(), result.getCreatedAt());
        assertEquals(wallet.getUpdatedAt(), result.getUpdatedAt());

        assertNull(result.getUserName());
        assertNull(result.getUserRole());

        verify(authorizationService).requireAdminWalletViewer(any(AuthenticatedUser.class));
        verify(walletRepository).findByUserId(userId);
        verifyNoInteractions(walletTransactionRepository);
    }

    @Test
    void getWalletByUserIdShouldThrowWalletNotFoundException() {
        UUID userId = UUID.randomUUID();

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(
                WalletNotFoundException.class,
                () -> walletService.getWalletByUserId(requester(UUID.randomUUID(), UserRole.ADMIN), userId)
        );

        verify(authorizationService).requireAdminWalletViewer(any(AuthenticatedUser.class));
        verify(walletRepository).findByUserId(userId);
        verifyNoInteractions(walletTransactionRepository);
    }

    @Test
    void getWalletByUserIdShouldThrowForbiddenWhenRequesterIsNotAdmin() {
        AuthenticatedUser requester = requester(UUID.randomUUID(), UserRole.BURUH);
        doThrow(new ForbiddenException())
                .when(authorizationService)
                .requireAdminWalletViewer(requester);

        assertThrows(
                ForbiddenException.class,
                () -> walletService.getWalletByUserId(requester, UUID.randomUUID())
        );

        verify(authorizationService).requireAdminWalletViewer(requester);
        verifyNoInteractions(walletRepository);
        verifyNoInteractions(walletTransactionRepository);
    }

    @Test
    void getMyTransactionsShouldReturnMappedPage() {
        UUID walletId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();

        Wallet wallet = createWallet(walletId, userId);
        WalletTransaction transaction = createTransaction(UUID.randomUUID(), walletId, referenceId);

        Pageable pageable = PageRequest.of(0, 20);
        Page<WalletTransaction> transactionPage = new PageImpl<>(List.of(transaction), pageable, 1);

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
        when(walletTransactionRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(transactionPage);

        Page<WalletTransactionResponse> result = walletService.getMyTransactions(
                requester(userId, UserRole.BURUH),
                new WalletTransactionFilter(TransactionType.CREDIT, null, null),
                pageable
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        WalletTransactionResponse response = result.getContent().getFirst();
        assertEquals(transaction.getId(), response.getId());
        assertEquals("CREDIT", response.getTransactionType());
        assertEquals(new BigDecimal("500.00"), response.getAmount());
        assertEquals(new BigDecimal("1000.00"), response.getBalanceBefore());
        assertEquals(new BigDecimal("1500.00"), response.getBalanceAfter());
        assertEquals("PAYROLL", response.getReferenceType());
        assertEquals(referenceId, response.getReferenceId());
        assertEquals("Payroll disbursement", response.getDescription());
        assertEquals(transaction.getCreatedAt(), response.getCreatedAt());

        verify(authorizationService).requireOwnWalletAccess(any(AuthenticatedUser.class));
        verify(walletRepository).findByUserId(userId);
        verify(walletTransactionRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getMyTransactionsShouldReturnEmptyPage() {
        UUID walletId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Wallet wallet = createWallet(walletId, userId);
        Pageable pageable = PageRequest.of(0, 20);
        Page<WalletTransaction> emptyPage = Page.empty(pageable);

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
        when(walletTransactionRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

        Page<WalletTransactionResponse> result = walletService.getMyTransactions(
                requester(userId, UserRole.BURUH),
                new WalletTransactionFilter(null, null, null),
                pageable
        );

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());

        verify(authorizationService).requireOwnWalletAccess(any(AuthenticatedUser.class));
        verify(walletRepository).findByUserId(userId);
        verify(walletTransactionRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void getMyTransactionsShouldBuildSpecificationWithAllFilters() {
        UUID walletId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Wallet wallet = createWallet(walletId, userId);
        Pageable pageable = PageRequest.of(0, 20);
        WalletTransactionFilter filter = new WalletTransactionFilter(
                TransactionType.DEBIT,
                OffsetDateTime.of(2026, 5, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 5, 21, 0, 0, 0, 0, ZoneOffset.UTC)
        );

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
        when(walletTransactionRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        walletService.getMyTransactions(requester(userId, UserRole.BURUH), filter, pageable);

        ArgumentCaptor<Specification<WalletTransaction>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(walletTransactionRepository).findAll(captor.capture(), eq(pageable));

        Root<WalletTransaction> root = mock(Root.class);
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
        verify(cb, times(2)).equal(any(Expression.class), any(Object.class));
        verify(cb).greaterThanOrEqualTo(any(Expression.class), eq(filter.dateFrom()));
        verify(cb).lessThan(any(Expression.class), eq(filter.dateTo()));
        verify(cb).and(any(Predicate[].class));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void getMyTransactionsShouldBuildSpecificationWithoutOptionalFilters() {
        UUID walletId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Wallet wallet = createWallet(walletId, userId);
        Pageable pageable = PageRequest.of(0, 20);

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
        when(walletTransactionRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        walletService.getMyTransactions(requester(userId, UserRole.BURUH), null, pageable);

        ArgumentCaptor<Specification<WalletTransaction>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(walletTransactionRepository).findAll(captor.capture(), eq(pageable));

        Root<WalletTransaction> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get(any(String.class))).thenReturn(path);
        when(cb.equal(any(Expression.class), any(Object.class))).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        Predicate result = captor.getValue().toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).equal(any(Expression.class), eq(walletId));
        verify(cb, never()).greaterThanOrEqualTo(any(Expression.class), any(OffsetDateTime.class));
        verify(cb, never()).lessThan(any(Expression.class), any(OffsetDateTime.class));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void getMyTransactionsShouldBuildSpecificationWithEmptyFilter() {
        UUID walletId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Wallet wallet = createWallet(walletId, userId);
        Pageable pageable = PageRequest.of(0, 20);

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
        when(walletTransactionRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        walletService.getMyTransactions(
                requester(userId, UserRole.BURUH),
                new WalletTransactionFilter(null, null, null),
                pageable
        );

        ArgumentCaptor<Specification<WalletTransaction>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(walletTransactionRepository).findAll(captor.capture(), eq(pageable));

        Root<WalletTransaction> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get(any(String.class))).thenReturn(path);
        when(cb.equal(any(Expression.class), any(Object.class))).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        Predicate result = captor.getValue().toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).equal(any(Expression.class), eq(walletId));
        verify(cb, never()).greaterThanOrEqualTo(any(Expression.class), any(OffsetDateTime.class));
        verify(cb, never()).lessThan(any(Expression.class), any(OffsetDateTime.class));
    }

    @Test
    void getMyTransactionsShouldThrowWalletNotFoundException() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(
                WalletNotFoundException.class,
                () -> walletService.getMyTransactions(
                        requester(userId, UserRole.BURUH),
                        new WalletTransactionFilter(null, null, null),
                        pageable
                )
        );

        verify(authorizationService).requireOwnWalletAccess(any(AuthenticatedUser.class));
        verify(walletRepository).findByUserId(userId);
        verifyNoInteractions(walletTransactionRepository);
    }

    @Test
    void getMyTransactionsShouldThrowForbiddenWhenRequesterIsNotAllowed() {
        AuthenticatedUser requester = requester(UUID.randomUUID(), UserRole.BURUH);
        Pageable pageable = PageRequest.of(0, 20);
        doThrow(new ForbiddenException())
                .when(authorizationService)
                .requireOwnWalletAccess(requester);

        assertThrows(
                ForbiddenException.class,
                () -> walletService.getMyTransactions(
                        requester,
                        new WalletTransactionFilter(null, null, null),
                        pageable
                )
        );

        verify(authorizationService).requireOwnWalletAccess(requester);
        verifyNoInteractions(walletRepository);
        verifyNoInteractions(walletTransactionRepository);
    }

    @Test
    void creditWalletShouldIncreaseBalanceAndSaveTransaction() {
        UUID walletId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();

        Wallet wallet = createWallet(walletId, userId);
        BigDecimal amount = new BigDecimal("500.00");

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));

        WalletMutationResult result = walletService.creditWallet(
                userId,
                amount,
                "PAYROLL_DISBURSEMENT",
                referenceId,
                "Payroll disbursement"
        );

        assertNotNull(result);
        assertEquals(new BigDecimal("1500.00"), result.getBalanceBefore());
        assertEquals(new BigDecimal("2000.00"), result.getBalanceAfter());

        assertEquals(new BigDecimal("2000.00"), wallet.getBalance());

        verify(walletRepository).findByUserId(userId);
        verify(walletRepository).save(wallet);

        ArgumentCaptor<WalletTransaction> transactionCaptor =
                ArgumentCaptor.forClass(WalletTransaction.class);

        verify(walletTransactionRepository).save(transactionCaptor.capture());

        WalletTransaction savedTransaction = transactionCaptor.getValue();
        assertEquals(walletId, savedTransaction.getWalletId());
        assertEquals(TransactionType.CREDIT, savedTransaction.getTransactionType());
        assertEquals(amount, savedTransaction.getAmount());
        assertEquals(new BigDecimal("1500.00"), savedTransaction.getBalanceBefore());
        assertEquals(new BigDecimal("2000.00"), savedTransaction.getBalanceAfter());
        assertEquals("PAYROLL_DISBURSEMENT", savedTransaction.getReferenceType());
        assertEquals(referenceId, savedTransaction.getReferenceId());
        assertEquals("Payroll disbursement", savedTransaction.getDescription());
    }

    @Test
    void creditWalletShouldThrowWalletNotFoundException() {
        UUID userId = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () -> walletService.creditWallet(
                userId,
                new BigDecimal("500.00"),
                "PAYROLL_DISBURSEMENT",
                referenceId,
                "Payroll disbursement"
        ));

        verify(walletRepository).findByUserId(userId);
        verifyNoInteractions(walletTransactionRepository);
    }

    @Test
    void debitWalletShouldDecreaseBalanceAndSaveTransaction() {
        UUID walletId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();

        Wallet wallet = createWallet(walletId, userId);
        BigDecimal amount = new BigDecimal("500.00");

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));

        WalletMutationResult result = walletService.debitWallet(
                userId,
                amount,
                "PAYROLL_DEDUCTION",
                referenceId,
                "Payroll deduction"
        );

        assertNotNull(result);
        assertEquals(new BigDecimal("1500.00"), result.getBalanceBefore());
        assertEquals(new BigDecimal("1000.00"), result.getBalanceAfter());

        assertEquals(new BigDecimal("1000.00"), wallet.getBalance());

        verify(walletRepository).findByUserId(userId);
        verify(walletRepository).save(wallet);

        ArgumentCaptor<WalletTransaction> transactionCaptor =
                ArgumentCaptor.forClass(WalletTransaction.class);

        verify(walletTransactionRepository).save(transactionCaptor.capture());

        WalletTransaction savedTransaction = transactionCaptor.getValue();
        assertEquals(walletId, savedTransaction.getWalletId());
        assertEquals(TransactionType.DEBIT, savedTransaction.getTransactionType());
        assertEquals(amount, savedTransaction.getAmount());
        assertEquals(new BigDecimal("1500.00"), savedTransaction.getBalanceBefore());
        assertEquals(new BigDecimal("1000.00"), savedTransaction.getBalanceAfter());
        assertEquals("PAYROLL_DEDUCTION", savedTransaction.getReferenceType());
        assertEquals(referenceId, savedTransaction.getReferenceId());
        assertEquals("Payroll deduction", savedTransaction.getDescription());
    }

    @Test
    void debitWalletShouldThrowWalletNotFoundException() {
        UUID userId = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () -> walletService.debitWallet(
                userId,
                new BigDecimal("500.00"),
                "PAYROLL_DEDUCTION",
                referenceId,
                "Payroll deduction"
        ));

        verify(walletRepository).findByUserId(userId);
        verifyNoInteractions(walletTransactionRepository);
    }

    @Test
    void createWalletShouldReturnExistingWalletWhenWalletAlreadyExists() {
        UUID walletId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        WalletCreationRequest request = mock(WalletCreationRequest.class);
        Wallet existingWallet = createWallet(walletId, userId);

        when(request.getUserId()).thenReturn(userId);
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(existingWallet));

        WalletCreationResponse result = walletService.createWallet(request);

        assertNotNull(result);
        assertEquals(walletId, result.getWalletId());
        assertTrue(result.isAlreadyProcessed());

        verify(request).getUserId();
        verify(walletRepository).findByUserId(userId);
        verify(walletRepository, never()).save(any(Wallet.class));
        verifyNoInteractions(walletTransactionRepository);
    }

    @Test
    void createWalletShouldCreateNewWalletWhenWalletDoesNotExist() {
        UUID walletId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        WalletCreationRequest request = mock(WalletCreationRequest.class);

        Wallet savedWallet = Wallet.builder()
                .id(walletId)
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .build();

        when(request.getUserId()).thenReturn(userId);
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class))).thenReturn(savedWallet);

        WalletCreationResponse result = walletService.createWallet(request);

        assertNotNull(result);
        assertEquals(walletId, result.getWalletId());
        assertFalse(result.isAlreadyProcessed());

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(walletCaptor.capture());

        Wallet walletToSave = walletCaptor.getValue();
        assertEquals(userId, walletToSave.getUserId());

        verify(request).getUserId();
        verify(walletRepository).findByUserId(userId);
        verifyNoInteractions(walletTransactionRepository);
    }
}
