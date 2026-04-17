package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class XenditInvoiceResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldDeserializeJsonCorrectly() throws Exception {
        String json = """
                {
                  "id": "inv-123",
                  "external_id": "ext-456",
                  "invoice_url": "https://xendit.co/invoice/123",
                  "status": "PENDING",
                  "expiry_date": "2026-04-17T10:15:30+07:00"
                }
                """;

        XenditInvoiceResponse response = objectMapper
                .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .readValue(json, XenditInvoiceResponse.class);

        assertThat(response.getId()).isEqualTo("inv-123");
        assertThat(response.getExternalId()).isEqualTo("ext-456");
        assertThat(response.getInvoiceUrl()).isEqualTo("https://xendit.co/invoice/123");
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getExpiryDate())
                .isEqualTo(OffsetDateTime.parse("2026-04-17T10:15:30+07:00"));
    }

    @Test
    void shouldSerializeObjectCorrectly() throws Exception {
        XenditInvoiceResponse response = new XenditInvoiceResponse();
        response.setId("inv-123");
        response.setExternalId("ext-456");
        response.setInvoiceUrl("https://xendit.co/invoice/123");
        response.setStatus("PENDING");
        response.setExpiryDate(
                OffsetDateTime.parse("2026-04-17T10:15:30+07:00")
        );

        String json = objectMapper
                .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .writeValueAsString(response);

        assertThat(json).contains("\"id\":\"inv-123\"");
        assertThat(json).contains("\"external_id\":\"ext-456\"");
        assertThat(json).contains("\"invoice_url\":\"https://xendit.co/invoice/123\"");
        assertThat(json).contains("\"status\":\"PENDING\"");
        assertThat(json).contains("\"expiry_date\":\"2026-04-17T10:15:30+07:00\"");
    }
}