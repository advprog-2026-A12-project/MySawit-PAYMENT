package id.ac.ui.cs.advprog.mysawitpayment.model;

import id.ac.ui.cs.advprog.mysawitpayment.model.enums.PayrollStatus;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.ReferenceType;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PayrollTest {

    private Payroll payroll;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();

        payroll = Payroll.builder()
                .userId(userId)
                .userRole(UserRole.BURUH)
                .kilogram(new BigDecimal("100"))
                .ratePerKg(new BigDecimal("5000"))
                .amount(new BigDecimal("450000"))
                .description("Payroll harvest payment")
                .referenceType(ReferenceType.HARVEST)
                .referenceId(referenceId)
                .build();
    }

    @Test
    void testDefaultValues() {
        assertEquals(new BigDecimal("0.90"), payroll.getMultiplier());
        assertEquals(PayrollStatus.PENDING, payroll.getStatus());
    }

    @Test
    void testIsPendingTrue() {
        assertTrue(payroll.isPending());
    }

    @Test
    void testAcceptPayroll() {
        UUID adminId = UUID.randomUUID();

        payroll.accept(adminId);

        assertEquals(PayrollStatus.ACCEPTED, payroll.getStatus());
        assertEquals(adminId, payroll.getApprovedBy());
        assertNotNull(payroll.getApprovedAt());
    }

    @Test
    void testRejectPayroll() {
        UUID adminId = UUID.randomUUID();
        String reason = "Invalid harvest data";

        payroll.reject(adminId, reason);

        assertEquals(PayrollStatus.REJECTED, payroll.getStatus());
        assertEquals(adminId, payroll.getApprovedBy());
        assertEquals(reason, payroll.getRejectionReason());
        assertNotNull(payroll.getApprovedAt());
    }

    @Test
    void testPrePersistSetsTimestamps() {
        payroll.prePersist();

        assertNotNull(payroll.getCreatedAt());
        assertNotNull(payroll.getUpdatedAt());
    }

    @Test
    void testPreUpdateUpdatesTimestamp() throws InterruptedException {
        payroll.prePersist();

        OffsetDateTime originalUpdatedAt = payroll.getUpdatedAt();

        Thread.sleep(5);

        payroll.preUpdate();

        assertTrue(payroll.getUpdatedAt().isAfter(originalUpdatedAt));
    }

    @Test
    void testIsPendingFalseAfterAccept() {
        payroll.accept(UUID.randomUUID());

        assertFalse(payroll.isPending());
    }

    @Test
    void testIsPendingFalseAfterReject() {
        payroll.reject(UUID.randomUUID(), "reason");

        assertFalse(payroll.isPending());
    }
}