package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WalletResponseTest {

    @Test
    void testGetterAndSetter() {
        WalletResponse walletResponse = new WalletResponse();
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BigDecimal balance = BigDecimal.valueOf(100);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        walletResponse.setId(id);
        walletResponse.setUserId(userId);
        walletResponse.setBalance(balance);
        walletResponse.setCurrency("SawitDollar");
        walletResponse.setCreatedAt(now);
        walletResponse.setUpdatedAt(now);

        assertEquals(id, walletResponse.getId());
        assertEquals(userId, walletResponse.getUserId());
        assertEquals(balance, walletResponse.getBalance());
        assertEquals("SawitDollar", walletResponse.getCurrency());
        assertEquals(now, walletResponse.getCreatedAt());
        assertEquals(now, walletResponse.getUpdatedAt());
    }
}
