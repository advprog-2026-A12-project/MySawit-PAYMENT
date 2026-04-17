package id.ac.ui.cs.advprog.mysawitpayment.exception;

public class PaymentTransactionAlreadyProcessedException extends RuntimeException {
    public PaymentTransactionAlreadyProcessedException(String message) {
        super(message);
    }

    public PaymentTransactionAlreadyProcessedException() {
        super("Payment transaction already processed");
    }
}
