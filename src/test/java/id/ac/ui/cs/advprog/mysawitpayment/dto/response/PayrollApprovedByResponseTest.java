package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PayrollApprovedByResponseTest {

    @Test
    void testGetterAndSetter() {
        PayrollApprovedByResponse response = new PayrollApprovedByResponse();

        UUID id = UUID.randomUUID();
        String name = "Admin Utama";

        response.setId(id);
        response.setName(name);

        assertEquals(id, response.getId());
        assertEquals(name, response.getName());
    }
}