package id.ac.ui.cs.advprog.mysawitpayment.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PayrollNotFoundExceptionTest {

    @Test
    void testDefaultConstructor() {
        PayrollNotFoundException exception = new PayrollNotFoundException();

        assertEquals("Payroll not found", exception.getMessage());
    }

    @Test
    void testCustomMessageConstructor() {
        String message = "Payroll with ID not found";

        PayrollNotFoundException exception =
                new PayrollNotFoundException(message);

        assertEquals(message, exception.getMessage());
    }
}