package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdminWalletResponseTest {

    @Test
    void testGetterAndSetter() {

        OffsetDateTime now = OffsetDateTime.now();

        AdminWalletResponse response = new AdminWalletResponse();
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BigDecimal balance = BigDecimal.valueOf(100);
        response.setUserName("admin");
        response.setId(id);
        response.setUserId(userId);
        response.setCreatedAt(now);
        response.setUpdatedAt(now);
        response.setUserRole("ADMIN");
        response.setBalance(balance);
        response.setCurrency("SawitDollar");

        assertEquals(id, response.getId());
        assertEquals(userId, response.getUserId());
        assertEquals(balance, response.getBalance());
        assertEquals("admin", response.getUserName());
        assertEquals("SawitDollar", response.getCurrency());
        assertEquals(now, response.getCreatedAt());
        assertEquals(now, response.getUpdatedAt());
        assertEquals("ADMIN", response.getUserRole());
    }
}
