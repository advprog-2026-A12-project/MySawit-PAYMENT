package id.ac.ui.cs.advprog.mysawitpayment.model;

import id.ac.ui.cs.advprog.mysawitpayment.model.enums.PayrollStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PayrollTest {

    @Test
    void payroll_settersAndGetters_workCorrectly() {
        Payroll payroll = new Payroll();

        LocalDateTime now = LocalDateTime.now();

        payroll.setId(1L);
        payroll.setUserId(10L);
        payroll.setKilogram(100.0);
        payroll.setAmount(90000.0);
        payroll.setStatus(PayrollStatus.PENDING);
        payroll.setReferenceId("123");
        payroll.setReferenceType("HARVEST");
        payroll.setCreatedAt(now);

        assertEquals(1L, payroll.getId());
        assertEquals(10L, payroll.getUserId());
        assertEquals(100.0, payroll.getKilogram());
        assertEquals(90000.0, payroll.getAmount());
        assertEquals(PayrollStatus.PENDING, payroll.getStatus());
        assertEquals("123", payroll.getReferenceId());
        assertEquals("HARVEST", payroll.getReferenceType());
        assertEquals(now, payroll.getCreatedAt());
    }

    @Test
    void payroll_defaultConstructor_createsObject() {
        Payroll payroll = new Payroll();
        assertNotNull(payroll);
    }

    @Test
    void payrollStatus_enumValues_exist() {
        assertEquals("PENDING", PayrollStatus.PENDING.name());
        assertEquals("ACCEPTED", PayrollStatus.ACCEPTED.name());
        assertEquals("REJECTED", PayrollStatus.REJECTED.name());
    }
}