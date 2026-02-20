package id.ac.ui.cs.advprog.mysawitpayment.dto.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CreatePayrollRequestTest {

    @Test
    void settersAndGettersWorkCorrectly() {
        CreatePayrollRequest request = new CreatePayrollRequest();

        request.setUserId(10L);
        request.setKilogram(100.0);
        request.setReferenceId("123");
        request.setReferenceType("HARVEST");

        assertEquals(10L, request.getUserId());
        assertEquals(100.0, request.getKilogram());
        assertEquals("123", request.getReferenceId());
        assertEquals("HARVEST", request.getReferenceType());
    }

    @Test
    void defaultConstructorCreatesObject() {
        CreatePayrollRequest request = new CreatePayrollRequest();
        assertNotNull(request);
    }
}