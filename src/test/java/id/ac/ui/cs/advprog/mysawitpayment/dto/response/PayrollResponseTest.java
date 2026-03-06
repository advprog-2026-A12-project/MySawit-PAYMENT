package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PayrollResponseTest {

    @Test
    void testGetterSetter() {

        PayrollResponse response = new PayrollResponse();

        UUID id = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100000");
        BigDecimal kilogram = new BigDecimal("50");
        BigDecimal ratePerKg = new BigDecimal("2000");
        BigDecimal multiplier = new BigDecimal("0.90");
        String status = "PENDING";
        String referenceType = "HARVEST";
        String description = "Payroll for harvest";
        OffsetDateTime approvedAt = OffsetDateTime.now();
        OffsetDateTime createdAt = OffsetDateTime.now();

        response.setId(id);
        response.setAmount(amount);
        response.setKilogram(kilogram);
        response.setRatePerKg(ratePerKg);
        response.setMultiplier(multiplier);
        response.setStatus(status);
        response.setReferenceType(referenceType);
        response.setDescription(description);
        response.setApprovedAt(approvedAt);
        response.setCreatedAt(createdAt);

        assertEquals(id, response.getId());
        assertEquals(amount, response.getAmount());
        assertEquals(kilogram, response.getKilogram());
        assertEquals(ratePerKg, response.getRatePerKg());
        assertEquals(multiplier, response.getMultiplier());
        assertEquals(status, response.getStatus());
        assertEquals(referenceType, response.getReferenceType());
        assertEquals(description, response.getDescription());
        assertEquals(approvedAt, response.getApprovedAt());
        assertEquals(createdAt, response.getCreatedAt());
    }
}