package id.ac.ui.cs.advprog.mysawitpayment.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InvalidAmountExceptionTest {

    @Test
    void testDefaultConstructor() {
        InvalidAmountException exception =
                new InvalidAmountException();

        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void testCustomMessageConstructor() {
        String message = "Invalid amount";

        InvalidAmountException exception =
                new InvalidAmountException(message);

        assertEquals(message, exception.getMessage());
    }
}
