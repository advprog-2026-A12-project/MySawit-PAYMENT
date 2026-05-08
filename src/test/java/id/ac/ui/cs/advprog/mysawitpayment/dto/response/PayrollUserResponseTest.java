package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PayrollUserResponseTest {

    @Test
    void testGetterAndSetter() {
        PayrollUserResponse response = new PayrollUserResponse();

        UUID id = UUID.randomUUID();
        String name = "Budi Mandor";
        String role = "MANDOR";

        response.setId(id);
        response.setName(name);
        response.setRole(role);

        assertEquals(id, response.getId());
        assertEquals(name, response.getName());
        assertEquals(role, response.getRole());
    }
}