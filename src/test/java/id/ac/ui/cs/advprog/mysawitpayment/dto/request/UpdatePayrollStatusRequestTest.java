package id.ac.ui.cs.advprog.mysawitpayment.dto.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UpdatePayrollStatusRequestTest {

    @Test
    void settersAndGetters_workCorrectly() {
        UpdatePayrollStatusRequest request = new UpdatePayrollStatusRequest();

        request.setReason("invalid data");

        assertEquals("invalid data", request.getReason());
    }

    @Test
    void defaultConstructor_createsObject() {
        UpdatePayrollStatusRequest request = new UpdatePayrollStatusRequest();
        assertNotNull(request);
    }
}