package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.dto.response.AdminWalletResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.WalletResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.WalletTransactionResponse;
import id.ac.ui.cs.advprog.mysawitpayment.exception.WalletNotFoundException;
import id.ac.ui.cs.advprog.mysawitpayment.model.Wallet;
import id.ac.ui.cs.advprog.mysawitpayment.model.WalletTransaction;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.TransactionType;
import id.ac.ui.cs.advprog.mysawitpayment.repository.WalletRepository;
import id.ac.ui.cs.advprog.mysawitpayment.repository.WalletTransactionRepository;
import id.ac.ui.cs.advprog.mysawitpayment.dto.result.WalletMutationResult;
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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

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

        WalletResponse result = walletService.getMyWallet(userId);

        assertNotNull(result);
        assertEquals(walletId, result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(new BigDecimal("1500.00"), result.getBalance());
        assertEquals("SawitDollar", result.getCurrency());
        assertEquals(wallet.getCreatedAt(), result.getCreatedAt());
        assertEquals(wallet.getUpdatedAt(), result.getUpdatedAt());

        verify(walletRepository).findByUserId(userId);
        verifyNoInteractions(walletTransactionRepository);
    }

    @Test
    void getMyWalletShouldThrowWalletNotFoundException() {
        UUID userId = UUID.randomUUID();

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () -> walletService.getMyWallet(userId));

        verify(walletRepository).findByUserId(userId);
        verifyNoInteractions(walletTransactionRepository);
    }

    @Test
    void getWalletByUserIdShouldReturnMappedResponse() {
        UUID walletId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Wallet wallet = createWallet(walletId, userId);

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));

        AdminWalletResponse result = walletService.getWalletByUserId(userId);

        assertNotNull(result);
        assertEquals(walletId, result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(new BigDecimal("1500.00"), result.getBalance());
        assertEquals("SawitDollar", result.getCurrency());
        assertEquals(wallet.getCreatedAt(), result.getCreatedAt());
        assertEquals(wallet.getUpdatedAt(), result.getUpdatedAt());

        assertNull(result.getUserName());
        assertNull(result.getUserRole());

        verify(walletRepository).findByUserId(userId);
        verifyNoInteractions(walletTransactionRepository);
    }

    @Test
    void getWalletByUserIdShouldThrowWalletNotFoundException() {
        UUID userId = UUID.randomUUID();

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () -> walletService.getWalletByUserId(userId));

        verify(walletRepository).findByUserId(userId);
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
        when(walletTransactionRepository.findByWalletId(walletId, pageable)).thenReturn(transactionPage);

        Page<WalletTransactionResponse> result = walletService.getMyTransactions(userId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        WalletTransactionResponse response = result.getContent().get(0);
        assertEquals(transaction.getId(), response.getId());
        assertEquals("CREDIT", response.getTransactionType());
        assertEquals(new BigDecimal("500.00"), response.getAmount());
        assertEquals(new BigDecimal("1000.00"), response.getBalanceBefore());
        assertEquals(new BigDecimal("1500.00"), response.getBalanceAfter());
        assertEquals("PAYROLL", response.getReferenceType());
        assertEquals(referenceId, response.getReferenceId());
        assertEquals("Payroll disbursement", response.getDescription());
        assertEquals(transaction.getCreatedAt(), response.getCreatedAt());

        verify(walletRepository).findByUserId(userId);
        verify(walletTransactionRepository).findByWalletId(walletId, pageable);
    }

    @Test
    void getMyTransactionsShouldReturnEmptyPage() {
        UUID walletId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Wallet wallet = createWallet(walletId, userId);
        Pageable pageable = PageRequest.of(0, 20);
        Page<WalletTransaction> emptyPage = Page.empty(pageable);

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
        when(walletTransactionRepository.findByWalletId(walletId, pageable)).thenReturn(emptyPage);

        Page<WalletTransactionResponse> result = walletService.getMyTransactions(userId, pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());

        verify(walletRepository).findByUserId(userId);
        verify(walletTransactionRepository).findByWalletId(walletId, pageable);
    }

    @Test
    void getMyTransactionsShouldThrowWalletNotFoundException() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () -> walletService.getMyTransactions(userId, pageable));

        verify(walletRepository).findByUserId(userId);
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
}