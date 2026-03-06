package id.ac.ui.cs.advprog.mysawitpayment.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WalletTest {

    private Wallet wallet;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();

        wallet = Wallet.builder()
                .userId(userId)
                .balance(new BigDecimal("1000.00"))
                .build();
    }

    @Test
    void testDefaultBalance() {
        Wallet newWallet = Wallet.builder()
                .userId(UUID.randomUUID())
                .build();

        assertEquals(BigDecimal.ZERO, newWallet.getBalance());
    }

    @Test
    void testPrePersistSetsTimestamps() {
        wallet.prePersist();

        assertNotNull(wallet.getCreatedAt());
        assertNotNull(wallet.getUpdatedAt());
        assertEquals(wallet.getCreatedAt(), wallet.getUpdatedAt());
    }

    @Test
    void testPrePersistSetsDefaultBalance() {
        Wallet newWallet = new Wallet();
        newWallet.setUserId(UUID.randomUUID());
        newWallet.prePersist();

        assertNotNull(newWallet.getBalance());
        assertEquals(BigDecimal.ZERO, newWallet.getBalance());
    }

    @Test
    void testPrePersistDoesNotOverrideExistingBalance() {
        BigDecimal existingBalance = new BigDecimal("500.00");
        wallet.setBalance(existingBalance);
        wallet.prePersist();

        assertEquals(existingBalance, wallet.getBalance());
    }

    @Test
    void testPreUpdateUpdatesTimestamp() throws InterruptedException {
        wallet.prePersist();

        OffsetDateTime originalUpdatedAt = wallet.getUpdatedAt();

        Thread.sleep(5);

        wallet.preUpdate();

        assertTrue(wallet.getUpdatedAt().isAfter(originalUpdatedAt));
    }

    @Test
    void testGettersAndSetters() {
        UUID userId = UUID.randomUUID();
        BigDecimal balance = new BigDecimal("2500.50");

        wallet.setUserId(userId);
        wallet.setBalance(balance);

        assertEquals(userId, wallet.getUserId());
        assertEquals(balance, wallet.getBalance());
    }

    @Test
    void testBalanceCanBeZero() {
        wallet.setBalance(BigDecimal.ZERO);

        assertEquals(BigDecimal.ZERO, wallet.getBalance());
    }

    @Test
    void testBuilderPattern() {
        UUID userId = UUID.randomUUID();
        BigDecimal balance = new BigDecimal("750.25");

        Wallet newWallet = Wallet.builder()
                .userId(userId)
                .balance(balance)
                .build();

        assertEquals(userId, newWallet.getUserId());
        assertEquals(balance, newWallet.getBalance());
    }
}