package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdminPayrollResponseTest {

    @Test
    void testGetterAndSetter() {
        AdminPayrollResponse response = new AdminPayrollResponse();

        UUID id = UUID.randomUUID();
        PayrollUserResponse user = new PayrollUserResponse();
        user.setId(UUID.randomUUID());
        user.setRole("MANDOR");

        BigDecimal amount = BigDecimal.valueOf(562.61);
        BigDecimal kilogram = BigDecimal.valueOf(250.50);
        BigDecimal ratePerKg = BigDecimal.valueOf(2.50);
        BigDecimal multiplier = BigDecimal.valueOf(0.90);
        String status = "PENDING";
        String referenceType = "HARVEST";
        UUID referenceId = UUID.randomUUID();
        String description = "Upah panen";
        OffsetDateTime createdAt = OffsetDateTime.now(ZoneOffset.UTC);

        response.setId(id);
        response.setUser(user);
        response.setAmount(amount);
        response.setKilogram(kilogram);
        response.setRatePerKg(ratePerKg);
        response.setMultiplier(multiplier);
        response.setStatus(status);
        response.setReferenceType(referenceType);
        response.setReferenceId(referenceId);
        response.setDescription(description);
        response.setCreatedAt(createdAt);

        assertEquals(id, response.getId());
        assertEquals(user, response.getUser());
        assertEquals(amount, response.getAmount());
        assertEquals(kilogram, response.getKilogram());
        assertEquals(ratePerKg, response.getRatePerKg());
        assertEquals(multiplier, response.getMultiplier());
        assertEquals(status, response.getStatus());
        assertEquals(referenceType, response.getReferenceType());
        assertEquals(referenceId, response.getReferenceId());
        assertEquals(description, response.getDescription());
        assertEquals(createdAt, response.getCreatedAt());
    }
}
