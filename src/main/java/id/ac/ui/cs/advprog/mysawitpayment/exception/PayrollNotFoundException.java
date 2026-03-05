package id.ac.ui.cs.advprog.mysawitpayment.exception;

public class PayrollNotFoundException extends RuntimeException {

    public PayrollNotFoundException() {
        super("Payroll not found");
    }

    public PayrollNotFoundException(String message) {
        super(message);
    }
}