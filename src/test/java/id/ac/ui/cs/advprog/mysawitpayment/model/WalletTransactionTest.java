package id.ac.ui.cs.advprog.mysawitpayment.model;

import id.ac.ui.cs.advprog.mysawitpayment.model.enums.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WalletTransactionTest {

    @Test
    void builderShouldSetFieldsCorrectly() {
        UUID id = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();

        WalletTransaction transaction = WalletTransaction.builder()
                .id(id)
                .walletId(walletId)
                .transactionType(TransactionType.CREDIT)
                .amount(new BigDecimal("50.00"))
                .balanceBefore(new BigDecimal("100.00"))
                .balanceAfter(new BigDecimal("150.00"))
                .referenceType("TOPUP")
                .referenceId(referenceId)
                .description("Top up wallet")
                .build();

        assertEquals(id, transaction.getId());
        assertEquals(walletId, transaction.getWalletId());
        assertEquals(TransactionType.CREDIT, transaction.getTransactionType());
        assertEquals(new BigDecimal("50.00"), transaction.getAmount());
        assertEquals(new BigDecimal("100.00"), transaction.getBalanceBefore());
        assertEquals(new BigDecimal("150.00"), transaction.getBalanceAfter());
        assertEquals("TOPUP", transaction.getReferenceType());
        assertEquals(referenceId, transaction.getReferenceId());
        assertEquals("Top up wallet", transaction.getDescription());
    }

    @Test
    void prePersistShouldSetCreatedAt() {
        WalletTransaction transaction = WalletTransaction.builder()
                .walletId(UUID.randomUUID())
                .transactionType(TransactionType.CREDIT)
                .amount(new BigDecimal("50.00"))
                .balanceBefore(new BigDecimal("100.00"))
                .balanceAfter(new BigDecimal("150.00"))
                .referenceType("TOPUP")
                .referenceId(UUID.randomUUID())
                .description("Top up wallet")
                .build();

        transaction.onCreate();

        assertNotNull(transaction.getCreatedAt());
    }

    @Test
    void prePersistShouldThrowExceptionWhenAmountIsNull() {
        WalletTransaction transaction = WalletTransaction.builder()
                .walletId(UUID.randomUUID())
                .transactionType(TransactionType.CREDIT)
                .amount(null)
                .balanceBefore(new BigDecimal("100.00"))
                .balanceAfter(new BigDecimal("150.00"))
                .referenceType("TOPUP")
                .referenceId(UUID.randomUUID())
                .description("Top up wallet")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                transaction::onCreate
        );

        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void prePersistShouldThrowExceptionWhenAmountIsZero() {
        WalletTransaction transaction = WalletTransaction.builder()
                .walletId(UUID.randomUUID())
                .transactionType(TransactionType.CREDIT)
                .amount(BigDecimal.ZERO)
                .balanceBefore(new BigDecimal("100.00"))
                .balanceAfter(new BigDecimal("100.00"))
                .referenceType("TOPUP")
                .referenceId(UUID.randomUUID())
                .description("Top up wallet")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                transaction::onCreate
        );

        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void prePersistShouldThrowExceptionWhenAmountIsNegative() {
        WalletTransaction transaction = WalletTransaction.builder()
                .walletId(UUID.randomUUID())
                .transactionType(TransactionType.DEBIT)
                .amount(new BigDecimal("-10.00"))
                .balanceBefore(new BigDecimal("100.00"))
                .balanceAfter(new BigDecimal("90.00"))
                .referenceType("PAYROLL_DEDUCTION")
                .referenceId(UUID.randomUUID())
                .description("Debit wallet")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                transaction::onCreate
        );

        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void prePersistShouldThrowExceptionWhenBalanceBeforeIsNull() {
        WalletTransaction transaction = WalletTransaction.builder()
                .walletId(UUID.randomUUID())
                .transactionType(TransactionType.CREDIT)
                .amount(new BigDecimal("50.00"))
                .balanceBefore(null)
                .balanceAfter(new BigDecimal("150.00"))
                .referenceType("TOPUP")
                .referenceId(UUID.randomUUID())
                .description("Top up wallet")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                transaction::onCreate
        );

        assertEquals("Balance cannot be null", exception.getMessage());
    }

    @Test
    void prePersistShouldThrowExceptionWhenBalanceAfterIsNull() {
        WalletTransaction transaction = WalletTransaction.builder()
                .walletId(UUID.randomUUID())
                .transactionType(TransactionType.CREDIT)
                .amount(new BigDecimal("50.00"))
                .balanceBefore(new BigDecimal("100.00"))
                .balanceAfter(null)
                .referenceType("TOPUP")
                .referenceId(UUID.randomUUID())
                .description("Top up wallet")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                transaction::onCreate
        );

        assertEquals("Balance cannot be null", exception.getMessage());
    }

    @Test
    void prePersistShouldThrowExceptionWhenBalanceAfterIsNegative() {
        WalletTransaction transaction = WalletTransaction.builder()
                .walletId(UUID.randomUUID())
                .transactionType(TransactionType.DEBIT)
                .amount(new BigDecimal("150.00"))
                .balanceBefore(new BigDecimal("100.00"))
                .balanceAfter(new BigDecimal("-50.00"))
                .referenceType("PAYROLL_DEDUCTION")
                .referenceId(UUID.randomUUID())
                .description("Debit wallet")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                transaction::onCreate
        );

        assertEquals("Balance after cannot be negative", exception.getMessage());
    }

    @Test
    void prePersistShouldKeepExistingId() {
        UUID existingId = UUID.randomUUID();

        WalletTransaction transaction = WalletTransaction.builder()
                .id(existingId)
                .walletId(UUID.randomUUID())
                .transactionType(TransactionType.CREDIT)
                .amount(new BigDecimal("50.00"))
                .balanceBefore(new BigDecimal("100.00"))
                .balanceAfter(new BigDecimal("150.00"))
                .referenceType("TOPUP")
                .referenceId(UUID.randomUUID())
                .description("Top up wallet")
                .build();

        transaction.onCreate();

        assertEquals(existingId, transaction.getId());
    }

    @Test
    void prePersistShouldSetCreatedAtWhenValid() {
        WalletTransaction transaction = WalletTransaction.builder()
                .walletId(UUID.randomUUID())
                .transactionType(TransactionType.DEBIT)
                .amount(new BigDecimal("25.00"))
                .balanceBefore(new BigDecimal("100.00"))
                .balanceAfter(new BigDecimal("75.00"))
                .referenceType("PAYROLL_DEDUCTION")
                .referenceId(UUID.randomUUID())
                .description("Debit wallet")
                .build();

        transaction.onCreate();

        assertNotNull(transaction.getId());
        assertNotNull(transaction.getCreatedAt());
        assertTrue(transaction.getCreatedAt().isBefore(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(1)));
    }
}