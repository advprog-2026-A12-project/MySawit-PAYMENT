package id.ac.ui.cs.advprog.mysawitpayment.exception;

import id.ac.ui.cs.advprog.mysawitpayment.dto.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
    void handleBadRequestShouldReturnBadRequestErrorResponse() {
        ResponseEntity<ApiResponse<Object>> response =
                handler.handleBadRequest(new InvalidAmountException("Invalid amount"));

        assertErrorResponse(response, HttpStatus.BAD_REQUEST, "Invalid amount");
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
        assertEquals(expectedStatus, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error", response.getBody().getStatus());
        assertEquals(expectedMessage, response.getBody().getMessage());
        assertNull(response.getBody().getData());
        assertNotNull(response.getBody().getTimestamp());
    }
}
