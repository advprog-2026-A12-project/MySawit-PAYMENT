package id.ac.ui.cs.advprog.mysawitpayment.exception;

public class PaymentTransactionNotFoundException extends RuntimeException {
    public PaymentTransactionNotFoundException(String message) {
        super(message);
    }

    public PaymentTransactionNotFoundException() {
        super("Payment transaction not found");
    }
}
