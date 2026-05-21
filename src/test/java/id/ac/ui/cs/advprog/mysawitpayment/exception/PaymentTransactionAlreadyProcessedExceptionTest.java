package id.ac.ui.cs.advprog.mysawitpayment.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaymentTransactionAlreadyProcessedExceptionTest {

    @Test
    void testDefaultConstructor() {
        PaymentTransactionAlreadyProcessedException exception =
                new PaymentTransactionAlreadyProcessedException();

        assertEquals("Payment transaction already processed", exception.getMessage());
    }

    @Test
    void testCustomMessageConstructor() {
        String message = "Transaction already processed";

        PaymentTransactionAlreadyProcessedException exception =
                new PaymentTransactionAlreadyProcessedException(message);

        assertEquals(message, exception.getMessage());
    }
}
