package id.ac.ui.cs.advprog.mysawitpayment.dto.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class XenditCallbackRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Test
    void shouldDeserializeCallbackJsonCorrectly() throws Exception {
        String json = """
                {
                  "id": "inv-123",
                  "external_id": "ext-456",
                  "status": "PAID",
                  "amount": 100000,
                  "paid_at": "2026-04-17T10:15:30+07:00"
                }
                """;

        XenditCallbackRequest request =
                objectMapper.readValue(json, XenditCallbackRequest.class);

        assertThat(request.getId()).isEqualTo("inv-123");
        assertThat(request.getExternalId()).isEqualTo("ext-456");
        assertThat(request.getStatus()).isEqualTo("PAID");
        assertThat(request.getAmount()).isEqualByComparingTo(new BigDecimal("100000"));
        assertThat(request.getPaidAt())
                .isEqualTo(OffsetDateTime.parse("2026-04-17T10:15:30+07:00"));
    }

    @Test
    void shouldThrowExceptionWhenDateInvalid() {
        String json = """
            {
              "paid_at": "invalid-date"
            }
            """;

        org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class,
                () -> objectMapper.readValue(json, XenditCallbackRequest.class)
        );
    }

    @Test
    void shouldHandleMissingFields() throws Exception {
        String json = """
            {
              "id": "inv-123",
              "external_id": "ext-456"
            }
            """;

        XenditCallbackRequest request =
                objectMapper.readValue(json, XenditCallbackRequest.class);

        assertThat(request.getStatus()).isNull();
        assertThat(request.getAmount()).isNull();
        assertThat(request.getPaidAt()).isNull();
    }
}