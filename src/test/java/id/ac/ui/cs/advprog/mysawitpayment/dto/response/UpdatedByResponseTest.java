package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UpdatedByResponseTest {

    @Test
    void shouldBuildCorrectly() {
        UUID id = UUID.randomUUID();

        UpdatedByResponse response = UpdatedByResponse.builder()
                .id(id)
                .build();

        assertEquals(id, response.getId());
    }
}
