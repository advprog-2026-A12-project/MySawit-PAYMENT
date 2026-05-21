package id.ac.ui.cs.advprog.mysawitpayment.exception;

import id.ac.ui.cs.advprog.mysawitpayment.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleForbiddenShouldReturnForbiddenErrorResponse() {
        ResponseEntity<ApiResponse<Object>> response =
                handler.handleForbidden(new ForbiddenException());

        assertErrorResponse(response, HttpStatus.FORBIDDEN, "Forbidden");
    }

    @Test
    void handleNotFoundShouldReturnNotFoundErrorResponse() {
        ResponseEntity<ApiResponse<Object>> response =
                handler.handleNotFound(new WalletNotFoundException());

        assertErrorResponse(response, HttpStatus.NOT_FOUND, "Wallet not found");
    }

    @Test
    void handleConflictShouldReturnConflictErrorResponse() {
        ResponseEntity<ApiResponse<Object>> response =
                handler.handleConflict(new PayrollAlreadyProcessedException());

        assertErrorResponse(response, HttpStatus.CONFLICT, "Payroll Already Processed");
    }

    @Test
    void handleConflictShouldReturnConflictForWageConfigRace() {
        ResponseEntity<ApiResponse<Object>> response =
                handler.handleConflict(new WageConfigConflictException());

        assertErrorResponse(response, HttpStatus.CONFLICT, "Active wage config was updated concurrently");
    }

    @Test
    void handleBadRequestShouldReturnBadRequestErrorResponse() {
        ResponseEntity<ApiResponse<Object>> response =
                handler.handleBadRequest(new InvalidAmountException("Invalid amount"));

        assertErrorResponse(response, HttpStatus.BAD_REQUEST, "Invalid amount");
    }

    @Test
    void handleValidationShouldReturnFieldErrors() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "amount", "must not be null"));
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ApiResponse<Object>> response = handler.handleValidation(exception);

        assertErrorResponse(response, HttpStatus.BAD_REQUEST, "Validation failed", false);
        assertFieldError(response, "amount", "must not be null");
    }

    @Test
    void handleConstraintViolationShouldReturnConstraintErrors() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("request.amount");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must be positive");

        ConstraintViolationException exception = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ApiResponse<Object>> response = handler.handleConstraintViolation(exception);

        assertErrorResponse(response, HttpStatus.BAD_REQUEST, "Validation failed", false);
        assertFieldError(response, "request.amount", "must be positive");
    }

    @Test
    void handleTypeMismatchShouldReturnBadRequest() {
        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
                "not-a-uuid",
                UUID.class,
                "id",
                null,
                new IllegalArgumentException("bad id")
        );

        ResponseEntity<ApiResponse<Object>> response = handler.handleTypeMismatch(exception);

        assertErrorResponse(response, HttpStatus.BAD_REQUEST, "Invalid request parameter");
    }

    @Test
    void handleUnreadableMessageShouldReturnBadRequest() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("bad json");

        ResponseEntity<ApiResponse<Object>> response = handler.handleUnreadableMessage(exception);

        assertErrorResponse(response, HttpStatus.BAD_REQUEST, "Malformed request body");
    }

    @Test
    void handleUnexpectedShouldReturnInternalServerErrorResponse() {
        ResponseEntity<ApiResponse<Object>> response =
                handler.handleUnexpected(new Exception("Unexpected"));

        assertErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }

    private void assertErrorResponse(
            ResponseEntity<ApiResponse<Object>> response,
            HttpStatus expectedStatus,
            String expectedMessage
    ) {
        assertErrorResponse(response, expectedStatus, expectedMessage, true);
    }

    private void assertErrorResponse(
            ResponseEntity<ApiResponse<Object>> response,
            HttpStatus expectedStatus,
            String expectedMessage,
            boolean expectNullData
    ) {
        assertEquals(expectedStatus, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error", response.getBody().getStatus());
        assertEquals(expectedMessage, response.getBody().getMessage());
        if (expectNullData) {
            assertNull(response.getBody().getData());
        } else {
            assertNotNull(response.getBody().getData());
        }
        assertNotNull(response.getBody().getTimestamp());
    }

    @SuppressWarnings("unchecked")
    private void assertFieldError(
            ResponseEntity<ApiResponse<Object>> response,
            String field,
            String message
    ) {
        Map<String, String> errors = (Map<String, String>) response.getBody().getData();

        assertEquals(message, errors.get(field));
    }
}
