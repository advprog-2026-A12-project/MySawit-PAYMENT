package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CurrentWageConfigResponseTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSerializeIsActiveCorrectly() throws Exception {
        CurrentWageConfigResponse response = CurrentWageConfigResponse.builder()
                .id(UUID.randomUUID())
                .upahBuruhPerKg(BigDecimal.valueOf(3.0))
                .upahSupirPerKg(BigDecimal.valueOf(2.0))
                .upahMandorPerKg(BigDecimal.valueOf(1.5))
                .currency("SawitDollar")
                .isActive(true)
                .updatedBy(UpdatedByResponse.builder()
                        .id(UUID.randomUUID())
                        .build())
                .effectiveFrom(OffsetDateTime.now(ZoneOffset.UTC))
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"isActive\":true");

        assertThat(json).doesNotContain("\"active\":");

        assertThat(json.split("isActive").length - 1).isEqualTo(1);
    }

    @Test
    void shouldSerializeAllFieldsCorrectly() throws Exception {
        UUID id = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        CurrentWageConfigResponse response = CurrentWageConfigResponse.builder()
                .id(id)
                .upahBuruhPerKg(BigDecimal.valueOf(3.0))
                .upahSupirPerKg(BigDecimal.valueOf(2.0))
                .upahMandorPerKg(BigDecimal.valueOf(1.5))
                .currency("SawitDollar")
                .isActive(false)
                .updatedBy(UpdatedByResponse.builder()
                        .id(adminId)
                        .build())
                .effectiveFrom(now)
                .createdAt(now)
                .build();

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains(id.toString());
        assertThat(json).contains("\"upahBuruhPerKg\":3.0");
        assertThat(json).contains("\"upahSupirPerKg\":2.0");
        assertThat(json).contains("\"upahMandorPerKg\":1.5");
        assertThat(json).contains("\"currency\":\"SawitDollar\"");
        assertThat(json).contains("\"isActive\":false");
        assertThat(json).contains(adminId.toString());
        assertThat(json).doesNotContain("\"name\":");
    }
}
