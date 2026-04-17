package id.ac.ui.cs.advprog.mysawitpayment.exception;

public class ActiveWageConfigNotFoundException extends RuntimeException {
    public ActiveWageConfigNotFoundException(String message) {
        super(message);
    }

    public ActiveWageConfigNotFoundException() {
        super("No active wage config found");
    }
}
