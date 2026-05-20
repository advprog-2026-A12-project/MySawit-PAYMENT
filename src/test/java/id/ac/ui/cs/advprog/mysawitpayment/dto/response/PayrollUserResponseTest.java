package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PayrollUserResponseTest {

    @Test
    void testGetterAndSetter() {
        PayrollUserResponse response = new PayrollUserResponse();

        UUID id = UUID.randomUUID();
        String role = "MANDOR";

        response.setId(id);
        response.setRole(role);

        assertEquals(id, response.getId());
        assertEquals(role, response.getRole());
    }
}
