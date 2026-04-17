package id.ac.ui.cs.advprog.mysawitpayment.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GatewayReferenceAlreadyAssignedExceptionTest {

    @Test
    void testDefaultConstructor() {
        GatewayReferenceAlreadyAssignedException exception =
                new GatewayReferenceAlreadyAssignedException();

        assertEquals("Gateway reference already assigned", exception.getMessage());
    }

    @Test
    void testCustomMessageConstructor() {
        String message = "Reference already assigned";

        GatewayReferenceAlreadyAssignedException exception =
                new GatewayReferenceAlreadyAssignedException(message);

        assertEquals(message, exception.getMessage());
    }
}
