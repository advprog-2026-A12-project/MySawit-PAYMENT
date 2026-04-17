package id.ac.ui.cs.advprog.mysawitpayment.exception;

public class InvalidAmountException extends RuntimeException {
    public InvalidAmountException(String message) {
      super(message);
    }

    public InvalidAmountException() {
      super("Amount must be greater than zero");
    }
}
