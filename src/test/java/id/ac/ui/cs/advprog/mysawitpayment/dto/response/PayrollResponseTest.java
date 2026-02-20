package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import id.ac.ui.cs.advprog.mysawitpayment.model.enums.PayrollStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PayrollResponseTest {

    @Test
    void settersAndGettersWorkCorrectly() {
        PayrollResponse response = new PayrollResponse();

        LocalDateTime now = LocalDateTime.now();

        response.setId(1L);
        response.setUserId(10L);
        response.setKilogram(100.0);
        response.setAmount(90000.0);
        response.setStatus(PayrollStatus.PENDING);
        response.setReferenceId("123");
        response.setReferenceType("HARVEST");
        response.setCreatedAt(now);

        assertEquals(1L, response.getId());
        assertEquals(10L, response.getUserId());
        assertEquals(100.0, response.getKilogram());
        assertEquals(90000.0, response.getAmount());
        assertEquals(PayrollStatus.PENDING, response.getStatus());
        assertEquals("123", response.getReferenceId());
        assertEquals("HARVEST", response.getReferenceType());
        assertEquals(now, response.getCreatedAt());
    }

    @Test
    void defaultConstructorCreatesObject() {
        PayrollResponse response = new PayrollResponse();
        assertNotNull(response);
    }
}