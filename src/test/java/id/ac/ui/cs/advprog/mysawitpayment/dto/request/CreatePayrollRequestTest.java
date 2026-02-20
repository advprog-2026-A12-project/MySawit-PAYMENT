package id.ac.ui.cs.advprog.mysawitpayment.dto.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreatePayrollRequestTest {

    @Test
    void settersAndGetters_workCorrectly() {
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
    void defaultConstructor_createsObject() {
        CreatePayrollRequest request = new CreatePayrollRequest();
        assertNotNull(request);
    }
}