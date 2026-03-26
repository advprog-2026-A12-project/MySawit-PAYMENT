package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.exception.InsufficientBalanceException;
import id.ac.ui.cs.advprog.mysawitpayment.exception.InvalidAmountException;
import id.ac.ui.cs.advprog.mysawitpayment.model.Wallet;
import id.ac.ui.cs.advprog.mysawitpayment.model.WalletTransaction;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.TransactionType;
import id.ac.ui.cs.advprog.mysawitpayment.repository.WalletRepository;
import id.ac.ui.cs.advprog.mysawitpayment.repository.WalletTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.never;


@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

    @Test
    void createWalletShouldCreateAndSaveWalletWhenUserDoesNotHaveWallet() {
        UUID userId = UUID.randomUUID();

        when(walletRepository.existsByUserId(userId)).thenReturn(false);
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Wallet result = walletService.createWallet(userId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(BigDecimal.ZERO, result.getBalance());

        verify(walletRepository).existsByUserId(userId);
        verify(walletRepository).save(any(Wallet.class));
        verifyNoInteractions(walletTransactionRepository);
    }

    @Test
    void createWalletShouldThrowExceptionWhenWalletAlreadyExists() {
        UUID userId = UUID.randomUUID();

        when(walletRepository.existsByUserId(userId)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> walletService.createWallet(userId)
        );

        assertEquals("Wallet already exists for user", exception.getMessage());

        verify(walletRepository).existsByUserId(userId);
        verify(walletRepository, never()).save(any(Wallet.class));
        verifyNoInteractions(walletTransactionRepository);
    }

    @Test
    void getWalletByUserIdShouldReturnWalletWhenFound() {
        UUID userId = UUID.randomUUID();
        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .balance(new BigDecimal("100.00"))
                .build();

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));

        Wallet result = walletService.getWalletByUserId(userId);

        assertNotNull(result);
        assertEquals(wallet.getId(), result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(new BigDecimal("100.00"), result.getBalance());

        verify(walletRepository).findByUserId(userId);
        verifyNoInteractions(walletTransactionRepository);
    }

    @Test
    void getWalletByUserIdShouldThrowExceptionWhenWalletNotFound() {
        UUID userId = UUID.randomUUID();

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> walletService.getWalletByUserId(userId)
        );

        assertEquals("Wallet not found", exception.getMessage());

        verify(walletRepository).findByUserId(userId);
        verifyNoInteractions(walletTransactionRepository);
    }

    @Test
    void creditShouldIncreaseBalanceAndSaveTransaction() {
        UUID userId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();

        Wallet wallet = Wallet.builder()
                .id(walletId)
                .userId(userId)
                .balance(new BigDecimal("100.00"))
                .build();

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletTransactionRepository.save(any(WalletTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        walletService.credit(userId, new BigDecimal("25.50"), "TOPUP", referenceId);

        assertEquals(new BigDecimal("125.50"), wallet.getBalance());

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(walletCaptor.capture());

        Wallet savedWallet = walletCaptor.getValue();
        assertEquals(walletId, savedWallet.getId());
        assertEquals(new BigDecimal("125.50"), savedWallet.getBalance());

        ArgumentCaptor<WalletTransaction> transactionCaptor =
                ArgumentCaptor.forClass(WalletTransaction.class);
        verify(walletTransactionRepository).save(transactionCaptor.capture());

        WalletTransaction savedTransaction = transactionCaptor.getValue();
        assertEquals(walletId, savedTransaction.getWalletId());
        assertEquals(TransactionType.CREDIT, savedTransaction.getTransactionType());
        assertEquals(new BigDecimal("25.50"), savedTransaction.getAmount());
        assertEquals(new BigDecimal("100.00"), savedTransaction.getBalanceBefore());
        assertEquals(new BigDecimal("125.50"), savedTransaction.getBalanceAfter());
        assertEquals("TOPUP", savedTransaction.getReferenceType());
        assertEquals(referenceId, savedTransaction.getReferenceId());
        assertEquals("Credit transaction", savedTransaction.getDescription());
    }

    @Test
    void creditShouldThrowExceptionWhenWalletNotFound() {
        UUID userId = UUID.randomUUID();

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> walletService.credit(userId, new BigDecimal("10.00"), "TOPUP", UUID.randomUUID())
        );

        assertEquals("Wallet not found", exception.getMessage());

        verify(walletRepository).findByUserId(userId);
        verify(walletRepository, never()).save(any(Wallet.class));
        verify(walletTransactionRepository, never()).save(any(WalletTransaction.class));
    }

    @Test
    void creditShouldThrowExceptionWhenAmountIsInvalid() {
        UUID userId = UUID.randomUUID();
        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .balance(new BigDecimal("100.00"))
                .build();

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));

        InvalidAmountException exception = assertThrows(
                InvalidAmountException.class,
                () -> walletService.credit(userId, BigDecimal.ZERO, "TOPUP", UUID.randomUUID())
        );

        assertEquals("Amount must be greater than zero", exception.getMessage());
        assertEquals(new BigDecimal("100.00"), wallet.getBalance());

        verify(walletRepository).findByUserId(userId);
        verify(walletRepository, never()).save(any(Wallet.class));
        verify(walletTransactionRepository, never()).save(any(WalletTransaction.class));
    }

    @Test
    void debitShouldDecreaseBalanceAndSaveTransaction() {
        UUID userId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();

        Wallet wallet = Wallet.builder()
                .id(walletId)
                .userId(userId)
                .balance(new BigDecimal("100.00"))
                .build();

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletTransactionRepository.save(any(WalletTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        walletService.debit(userId, new BigDecimal("40.00"), "PAYROLL_DEDUCTION", referenceId);

        assertEquals(new BigDecimal("60.00"), wallet.getBalance());

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(walletCaptor.capture());

        Wallet savedWallet = walletCaptor.getValue();
        assertEquals(walletId, savedWallet.getId());
        assertEquals(new BigDecimal("60.00"), savedWallet.getBalance());

        ArgumentCaptor<WalletTransaction> transactionCaptor =
                ArgumentCaptor.forClass(WalletTransaction.class);
        verify(walletTransactionRepository).save(transactionCaptor.capture());

        WalletTransaction savedTransaction = transactionCaptor.getValue();
        assertEquals(walletId, savedTransaction.getWalletId());
        assertEquals(TransactionType.DEBIT, savedTransaction.getTransactionType());
        assertEquals(new BigDecimal("40.00"), savedTransaction.getAmount());
        assertEquals(new BigDecimal("100.00"), savedTransaction.getBalanceBefore());
        assertEquals(new BigDecimal("60.00"), savedTransaction.getBalanceAfter());
        assertEquals("PAYROLL_DEDUCTION", savedTransaction.getReferenceType());
        assertEquals(referenceId, savedTransaction.getReferenceId());
        assertEquals("Debit transaction", savedTransaction.getDescription());
    }

    @Test
    void debitShouldThrowExceptionWhenBalanceIsInsufficient() {
        UUID userId = UUID.randomUUID();
        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .balance(new BigDecimal("30.00"))
                .build();

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));

        InsufficientBalanceException exception = assertThrows(
                InsufficientBalanceException.class,
                () -> walletService.debit(userId, new BigDecimal("50.00"), "PAYROLL_DEDUCTION", UUID.randomUUID())
        );

        assertEquals("Insufficient wallet balance", exception.getMessage());
        assertEquals(new BigDecimal("30.00"), wallet.getBalance());

        verify(walletRepository).findByUserId(userId);
        verify(walletRepository, never()).save(any(Wallet.class));
        verify(walletTransactionRepository, never()).save(any(WalletTransaction.class));
    }

    @Test
    void debitShouldThrowExceptionWhenWalletNotFound() {
        UUID userId = UUID.randomUUID();

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> walletService.debit(userId, new BigDecimal("10.00"), "PAYROLL_DEDUCTION", UUID.randomUUID())
        );

        assertEquals("Wallet not found", exception.getMessage());

        verify(walletRepository).findByUserId(userId);
        verify(walletRepository, never()).save(any(Wallet.class));
        verify(walletTransactionRepository, never()).save(any(WalletTransaction.class));
    }
}