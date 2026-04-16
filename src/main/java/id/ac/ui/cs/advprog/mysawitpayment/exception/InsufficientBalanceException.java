package id.ac.ui.cs.advprog.mysawitpayment.exception;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String message) {
      super(message);
    }

    public InsufficientBalanceException() {
      super("Insufficient wallet balance");
    }
}
