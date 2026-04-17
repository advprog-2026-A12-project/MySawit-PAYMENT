package id.ac.ui.cs.advprog.mysawitpayment.exception;

public class GatewayReferenceAlreadyAssignedException extends RuntimeException {
    public GatewayReferenceAlreadyAssignedException(String message) {
        super(message);
    }

    public GatewayReferenceAlreadyAssignedException() {
        super("Gateway reference already assigned");
    }
}
