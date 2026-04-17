package id.ac.ui.cs.advprog.mysawitpayment.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateWageConfigRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassValidationWhenAllFieldsValid() {
        CreateWageConfigRequest request = new CreateWageConfigRequest();
        request.setUpahBuruhPerKg(BigDecimal.valueOf(3.0));
        request.setUpahSupirPerKg(BigDecimal.valueOf(2.0));
        request.setUpahMandorPerKg(BigDecimal.valueOf(1.5));

        Set<ConstraintViolation<CreateWageConfigRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenFieldsAreNull() {
        CreateWageConfigRequest request = new CreateWageConfigRequest();

        Set<ConstraintViolation<CreateWageConfigRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenValueLessThanMinimum() {
        CreateWageConfigRequest request = new CreateWageConfigRequest();
        request.setUpahBuruhPerKg(BigDecimal.ZERO);
        request.setUpahSupirPerKg(BigDecimal.valueOf(-1));
        request.setUpahMandorPerKg(BigDecimal.valueOf(0.009));

        Set<ConstraintViolation<CreateWageConfigRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
    }
}