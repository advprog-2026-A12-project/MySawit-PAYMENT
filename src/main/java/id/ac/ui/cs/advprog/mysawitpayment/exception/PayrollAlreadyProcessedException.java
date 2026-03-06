package id.ac.ui.cs.advprog.mysawitpayment.exception;

public class PayrollAlreadyProcessedException extends RuntimeException {

    public PayrollAlreadyProcessedException() {
        super("Payroll Already Processed");
    }

    public PayrollAlreadyProcessedException(String message) {
        super(message);
    }
}
