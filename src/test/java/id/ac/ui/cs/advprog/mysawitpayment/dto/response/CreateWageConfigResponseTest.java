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
class CreateWageConfigResponseTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSerializeIsActiveCorrectly() throws Exception {
        CreateWageConfigResponse response = CreateWageConfigResponse.builder()
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
    void shouldSerializeNestedObjectsCorrectly() throws Exception {
        UUID prevId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        CreateWageConfigResponse response = CreateWageConfigResponse.builder()
                .id(UUID.randomUUID())
                .upahBuruhPerKg(BigDecimal.valueOf(3.0))
                .upahSupirPerKg(BigDecimal.valueOf(2.0))
                .upahMandorPerKg(BigDecimal.valueOf(1.5))
                .currency("SawitDollar")
                .isActive(true)
                .previousConfig(PreviousWageConfigResponse.builder()
                        .id(prevId)
                        .upahBuruhPerKg(BigDecimal.valueOf(2.5))
                        .upahSupirPerKg(BigDecimal.valueOf(1.5))
                        .upahMandorPerKg(BigDecimal.valueOf(1.0))
                        .deactivatedAt(OffsetDateTime.now(ZoneOffset.UTC))
                        .build())
                .updatedBy(UpdatedByResponse.builder()
                        .id(adminId)
                        .build())
                .effectiveFrom(OffsetDateTime.now(ZoneOffset.UTC))
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"previousConfig\"");
        assertThat(json).contains(prevId.toString());

        assertThat(json).contains("\"updatedBy\"");
        assertThat(json).contains(adminId.toString());
        assertThat(json).doesNotContain("\"name\":");
    }
}
