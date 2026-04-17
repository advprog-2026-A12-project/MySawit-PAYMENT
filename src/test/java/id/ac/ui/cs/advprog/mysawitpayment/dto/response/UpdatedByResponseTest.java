package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UpdatedByResponseTest {

    @Test
    void shouldBuildCorrectly() {
        UUID id = UUID.randomUUID();
        String name = "Admin";

        UpdatedByResponse response = UpdatedByResponse.builder()
                .id(id)
                .name(name)
                .build();

        assertEquals(id, response.getId());
        assertEquals(name, response.getName());
    }
}