package id.ac.ui.cs.advprog.mysawitpayment.dto.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UpdatePayrollStatusRequestTest {

    @Test
    void settersAndGettersWorkCorrectly() {
        UpdatePayrollStatusRequest request = new UpdatePayrollStatusRequest();

        request.setReason("invalid data");

        assertEquals("invalid data", request.getReason());
    }

    @Test
    void defaultConstructorCreatesObject() {
        UpdatePayrollStatusRequest request = new UpdatePayrollStatusRequest();
        assertNotNull(request);
    }
}