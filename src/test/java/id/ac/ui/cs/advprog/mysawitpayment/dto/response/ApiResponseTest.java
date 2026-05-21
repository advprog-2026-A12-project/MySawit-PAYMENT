package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiResponseTest {

    @Test
    void testBuilderAndGetter() {

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .status("success")
                .message("Data retrieved")
                .data("payload")
                .timestamp(now)
                .build();

        assertEquals("success", response.getStatus());
        assertEquals("Data retrieved", response.getMessage());
        assertEquals("payload", response.getData());
        assertEquals(now, response.getTimestamp());
    }
}