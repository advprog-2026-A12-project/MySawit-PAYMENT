package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WalletTransactionResponseTest {

    @Test
    void testGetterAndSetter() {
        WalletTransactionResponse walletTransactionResponse = new WalletTransactionResponse();
        UUID id = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(100);
        BigDecimal balanceBefore = BigDecimal.valueOf(200);
        BigDecimal balanceAfter = BigDecimal.valueOf(100);
        OffsetDateTime now = OffsetDateTime.now();
        walletTransactionResponse.setId(id);
        walletTransactionResponse.setTransactionType("CREDIT");
        walletTransactionResponse.setAmount(amount);
        walletTransactionResponse.setBalanceBefore(balanceBefore);
        walletTransactionResponse.setBalanceAfter(balanceAfter);
        walletTransactionResponse.setReferenceType("PAYROLL_DISBURSEMENT");
        walletTransactionResponse.setCreatedAt(now);
        walletTransactionResponse.setReferenceId(referenceId);
        walletTransactionResponse.setDescription("Halo");

        assertEquals(id, walletTransactionResponse.getId());
        assertEquals(referenceId, walletTransactionResponse.getReferenceId());
        assertEquals(amount, walletTransactionResponse.getAmount());
        assertEquals(balanceBefore, walletTransactionResponse.getBalanceBefore());
        assertEquals(balanceAfter, walletTransactionResponse.getBalanceAfter());
        assertEquals(now, walletTransactionResponse.getCreatedAt());
        assertEquals("PAYROLL_DISBURSEMENT", walletTransactionResponse.getReferenceType());
        assertEquals("Halo", walletTransactionResponse.getDescription());
        assertEquals("CREDIT", walletTransactionResponse.getTransactionType());
    }
}
