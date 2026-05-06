package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PayrollDetailResponseTest {

    @Test
    void testGetterAndSetter() {
        PayrollDetailResponse response = new PayrollDetailResponse();

        UUID id = UUID.randomUUID();

        PayrollUserResponse user = new PayrollUserResponse();
        user.setId(UUID.randomUUID());
        user.setName("Ahmad Buruh");
        user.setRole("BURUH");

        BigDecimal amount = BigDecimal.valueOf(562.61);
        BigDecimal kilogram = BigDecimal.valueOf(250.50);
        BigDecimal ratePerKg = BigDecimal.valueOf(2.50);
        BigDecimal multiplier = BigDecimal.valueOf(0.90);

        String status = "PENDING";
        String description = "Upah panen";
        String rejectionReason = "Data tidak valid";
        String referenceType = "HARVEST";
        UUID referenceId = UUID.randomUUID();

        PayrollApprovedByResponse approvedBy = new PayrollApprovedByResponse();
        approvedBy.setId(UUID.randomUUID());
        approvedBy.setName("Admin Utama");

        OffsetDateTime approvedAt = OffsetDateTime.now();
        OffsetDateTime createdAt = OffsetDateTime.now();
        OffsetDateTime updatedAt = OffsetDateTime.now();

        response.setId(id);
        response.setUser(user);
        response.setAmount(amount);
        response.setKilogram(kilogram);
        response.setRatePerKg(ratePerKg);
        response.setMultiplier(multiplier);
        response.setStatus(status);
        response.setDescription(description);
        response.setRejectionReason(rejectionReason);
        response.setReferenceType(referenceType);
        response.setReferenceId(referenceId);
        response.setApprovedBy(approvedBy);
        response.setApprovedAt(approvedAt);
        response.setCreatedAt(createdAt);
        response.setUpdatedAt(updatedAt);

        assertEquals(id, response.getId());
        assertEquals(user, response.getUser());
        assertEquals(amount, response.getAmount());
        assertEquals(kilogram, response.getKilogram());
        assertEquals(ratePerKg, response.getRatePerKg());
        assertEquals(multiplier, response.getMultiplier());
        assertEquals(status, response.getStatus());
        assertEquals(description, response.getDescription());
        assertEquals(rejectionReason, response.getRejectionReason());
        assertEquals(referenceType, response.getReferenceType());
        assertEquals(referenceId, response.getReferenceId());
        assertEquals(approvedBy, response.getApprovedBy());
        assertEquals(approvedAt, response.getApprovedAt());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(updatedAt, response.getUpdatedAt());
    }
}