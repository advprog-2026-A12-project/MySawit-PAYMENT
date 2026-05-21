package id.ac.ui.cs.advprog.mysawitpayment.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InsufficientBalanceExceptionTest {

    @Test
    void testDefaultConstructor() {
        InsufficientBalanceException exception =
                new InsufficientBalanceException();

        assertEquals("Insufficient wallet balance", exception.getMessage());
    }

    @Test
    void testCustomMessageConstructor() {
        String message = "Insufficient balance";

        InsufficientBalanceException exception =
                new InsufficientBalanceException(message);

        assertEquals(message, exception.getMessage());
    }
}
