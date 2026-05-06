package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RejectPayrollResponseTest {

    @Test
    void testGetterAndSetter() {
        RejectPayrollResponse response = new RejectPayrollResponse();

        UUID id = UUID.randomUUID();

        PayrollUserResponse user = new PayrollUserResponse();
        user.setId(UUID.randomUUID());
        user.setName("Ahmad Buruh");
        user.setRole("BURUH");

        BigDecimal amount = BigDecimal.valueOf(562.61);
        String status = "REJECTED";
        String rejectionReason = "Data kilogram tidak sesuai";

        PayrollApprovedByResponse approvedBy = new PayrollApprovedByResponse();
        approvedBy.setId(UUID.randomUUID());
        approvedBy.setName("Admin Utama");

        OffsetDateTime approvedAt = OffsetDateTime.now();

        response.setId(id);
        response.setUser(user);
        response.setAmount(amount);
        response.setStatus(status);
        response.setRejectionReason(rejectionReason);
        response.setApprovedBy(approvedBy);
        response.setApprovedAt(approvedAt);

        assertEquals(id, response.getId());
        assertEquals(user, response.getUser());
        assertEquals(amount, response.getAmount());
        assertEquals(status, response.getStatus());
        assertEquals(rejectionReason, response.getRejectionReason());
        assertEquals(approvedBy, response.getApprovedBy());
        assertEquals(approvedAt, response.getApprovedAt());
    }
}