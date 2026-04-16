package id.ac.ui.cs.advprog.mysawitpayment.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WalletNotFoundExceptionTest {

    @Test
    void testDefaultConstructor() {
        WalletNotFoundException exception = new WalletNotFoundException();

        assertEquals("Wallet not found", exception.getMessage());
    }

    @Test
    void testCustomMessageConstructor() {
        String message = "Wallet with ID not found";

        WalletNotFoundException exception =
                new WalletNotFoundException(message);

        assertEquals(message, exception.getMessage());
    }
}
