package id.ac.ui.cs.advprog.mysawitpayment.exception;

public class WageConfigConflictException extends RuntimeException {

    public WageConfigConflictException() {
        super("Active wage config was updated concurrently");
    }
}
