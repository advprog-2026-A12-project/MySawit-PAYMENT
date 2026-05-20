package id.ac.ui.cs.advprog.mysawitpayment.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class RejectPayrollRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidRejectionReason() {
        RejectPayrollRequest request = new RejectPayrollRequest();
        request.setRejectionReason("Reason more than ten");

        Set<ConstraintViolation<RejectPayrollRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidRejectionReasonTooShort() {
        RejectPayrollRequest request = new RejectPayrollRequest();
        request.setRejectionReason("short");

        Set<ConstraintViolation<RejectPayrollRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void testInvalidRejectionReasonNull() {
        RejectPayrollRequest request = new RejectPayrollRequest();

        Set<ConstraintViolation<RejectPayrollRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void testInvalidRejectionReasonBlank() {
        RejectPayrollRequest request = new RejectPayrollRequest();
        request.setRejectionReason("   ");

        Set<ConstraintViolation<RejectPayrollRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void testGetterSetter() {
        RejectPayrollRequest request = new RejectPayrollRequest();

        request.setRejectionReason("This is a valid rejection reason");

        assertEquals(
                "This is a valid rejection reason",
                request.getRejectionReason()
        );
    }
}
