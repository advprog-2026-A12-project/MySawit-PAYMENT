package id.ac.ui.cs.advprog.mysawitpayment.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PayrollAlreadyProcessedExceptionTest {

    @Test
    void testDefaultConstructor() {
        PayrollAlreadyProcessedException exception =
                new PayrollAlreadyProcessedException();

        assertEquals("Payroll Already Processed", exception.getMessage());
    }

    @Test
    void testCustomMessageConstructor() {
        String message = "Payroll already accepted";

        PayrollAlreadyProcessedException exception =
                new PayrollAlreadyProcessedException(message);

        assertEquals(message, exception.getMessage());
    }
}