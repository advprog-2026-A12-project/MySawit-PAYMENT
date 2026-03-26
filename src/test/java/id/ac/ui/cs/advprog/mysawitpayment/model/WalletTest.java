package id.ac.ui.cs.advprog.mysawitpayment.model;

import id.ac.ui.cs.advprog.mysawitpayment.exception.InsufficientBalanceException;
import id.ac.ui.cs.advprog.mysawitpayment.exception.InvalidAmountException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WalletTest {

    @Test
    void builderShouldSetDefaultValues() {
        UUID userId = UUID.randomUUID();

        Wallet wallet = Wallet.builder()
                .userId(userId)
                .build();

        assertNotNull(wallet.getId());
        assertEquals(userId, wallet.getUserId());
        assertEquals(BigDecimal.ZERO, wallet.getBalance());
    }

    @Test
    void prePersistShouldSetCreatedAtUpdatedAtAndDefaultBalance() {
        Wallet wallet = Wallet.builder()
                .userId(UUID.randomUUID())
                .balance(null)
                .build();

        wallet.prePersist();

        assertNotNull(wallet.getId());
        assertNotNull(wallet.getCreatedAt());
        assertNotNull(wallet.getUpdatedAt());
        assertEquals(BigDecimal.ZERO, wallet.getBalance());
        assertEquals(wallet.getCreatedAt(), wallet.getUpdatedAt());
    }

    @Test
    void prePersistShouldThrowExceptionWhenBalanceIsNegative() {
        Wallet wallet = Wallet.builder()
                .userId(UUID.randomUUID())
                .balance(new BigDecimal("-1.00"))
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                wallet::prePersist
        );

        assertEquals("Balance cannot be negative", exception.getMessage());
    }

    @Test
    void preUpdateShouldRefreshUpdatedAt() {
        Wallet wallet = Wallet.builder()
                .userId(UUID.randomUUID())
                .balance(new BigDecimal("100.00"))
                .createdAt(OffsetDateTime.parse("2024-01-01T00:00:00Z"))
                .updatedAt(OffsetDateTime.parse("2024-01-01T00:00:00Z"))
                .build();

        OffsetDateTime oldUpdatedAt = wallet.getUpdatedAt();

        wallet.preUpdate();

        assertNotNull(wallet.getUpdatedAt());
        assertTrue(wallet.getUpdatedAt().isAfter(oldUpdatedAt));
    }

    @Test
    void preUpdateShouldThrowExceptionWhenBalanceIsNegative() {
        Wallet invalidWallet = Wallet.builder()
                .userId(UUID.randomUUID())
                .balance(new BigDecimal("-10.00"))
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                invalidWallet::preUpdate
        );

        assertEquals("Balance cannot be negative", exception.getMessage());
    }

    @Test
    void creditShouldIncreaseBalance() {
        Wallet wallet = Wallet.builder()
                .userId(UUID.randomUUID())
                .balance(new BigDecimal("100.00"))
                .build();

        wallet.credit(new BigDecimal("25.50"));

        assertEquals(new BigDecimal("125.50"), wallet.getBalance());
    }

    @Test
    void creditShouldThrowExceptionWhenAmountIsNull() {
        Wallet wallet = Wallet.builder()
                .userId(UUID.randomUUID())
                .balance(new BigDecimal("100.00"))
                .build();

        InvalidAmountException exception = assertThrows(
                InvalidAmountException.class,
                () -> wallet.credit(null)
        );

        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void creditShouldThrowExceptionWhenAmountIsZero() {
        Wallet wallet = Wallet.builder()
                .userId(UUID.randomUUID())
                .balance(new BigDecimal("100.00"))
                .build();

        InvalidAmountException exception = assertThrows(
                InvalidAmountException.class,
                () -> wallet.credit(BigDecimal.ZERO)
        );

        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void debitShouldDecreaseBalance() {
        Wallet wallet = Wallet.builder()
                .userId(UUID.randomUUID())
                .balance(new BigDecimal("100.00"))
                .build();

        wallet.debit(new BigDecimal("40.00"));

        assertEquals(new BigDecimal("60.00"), wallet.getBalance());
    }

    @Test
    void debitShouldThrowExceptionWhenAmountIsNull() {
        Wallet wallet = Wallet.builder()
                .userId(UUID.randomUUID())
                .balance(new BigDecimal("100.00"))
                .build();

        InvalidAmountException exception = assertThrows(
                InvalidAmountException.class,
                () -> wallet.debit(null)
        );

        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void debitShouldThrowExceptionWhenAmountIsZero() {
        Wallet wallet = Wallet.builder()
                .userId(UUID.randomUUID())
                .balance(new BigDecimal("100.00"))
                .build();

        InvalidAmountException exception = assertThrows(
                InvalidAmountException.class,
                () -> wallet.debit(BigDecimal.ZERO)
        );

        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void debitShouldThrowExceptionWhenBalanceIsInsufficient() {
        Wallet wallet = Wallet.builder()
                .userId(UUID.randomUUID())
                .balance(new BigDecimal("50.00"))
                .build();

        InsufficientBalanceException exception = assertThrows(
                InsufficientBalanceException.class,
                () -> wallet.debit(new BigDecimal("60.00"))
        );

        assertEquals("Insufficient wallet balance", exception.getMessage());
        assertEquals(new BigDecimal("50.00"), wallet.getBalance());
    }
}