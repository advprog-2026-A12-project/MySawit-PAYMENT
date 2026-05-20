package id.ac.ui.cs.advprog.mysawitpayment.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateTopUpRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassValidationWhenAmountValid() {
        CreateTopUpRequest request = new CreateTopUpRequest();

        setField(request, "amountSawitDollar", new BigDecimal("100"));

        Set<ConstraintViolation<CreateTopUpRequest>> violations =
                validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailWhenAmountIsNull() {
        CreateTopUpRequest request = new CreateTopUpRequest();

        Set<ConstraintViolation<CreateTopUpRequest>> violations =
                validator.validate(request);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldFailWhenAmountIsNegative() {
        CreateTopUpRequest request = new CreateTopUpRequest();

        setField(request, "amountSawitDollar", new BigDecimal("-10"));

        Set<ConstraintViolation<CreateTopUpRequest>> violations =
                validator.validate(request);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldFailWhenAmountIsZero() {
        CreateTopUpRequest request = new CreateTopUpRequest();

        setField(request, "amountSawitDollar", BigDecimal.ZERO);

        Set<ConstraintViolation<CreateTopUpRequest>> violations =
                validator.validate(request);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldFailWhenAmountExceedsMaximum() {
        CreateTopUpRequest request = new CreateTopUpRequest();

        setField(request, "amountSawitDollar", new BigDecimal("100000.01"));

        Set<ConstraintViolation<CreateTopUpRequest>> violations =
                validator.validate(request);

        assertThat(violations).isNotEmpty();
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
