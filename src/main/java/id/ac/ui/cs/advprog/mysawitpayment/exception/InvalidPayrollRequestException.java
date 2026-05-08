package id.ac.ui.cs.advprog.mysawitpayment.exception;

public class InvalidPayrollRequestException extends RuntimeException {
    public InvalidPayrollRequestException(String message) {
        super(message);
    }

    public InvalidPayrollRequestException() {
        super("Invalid payroll request");
    }
}
