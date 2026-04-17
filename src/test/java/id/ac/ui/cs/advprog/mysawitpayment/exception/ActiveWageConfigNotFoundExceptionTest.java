package id.ac.ui.cs.advprog.mysawitpayment.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActiveWageConfigNotFoundExceptionTest {
    @Test
    void testDefaultConstructor() {
        ActiveWageConfigNotFoundException exception =
                new ActiveWageConfigNotFoundException();

        assertEquals("No active wage config found", exception.getMessage());
    }

    @Test
    void testCustomMessageConstructor() {
        String message = "No active config";

        ActiveWageConfigNotFoundException exception =
                new ActiveWageConfigNotFoundException(message);

        assertEquals(message, exception.getMessage());
    }
}
