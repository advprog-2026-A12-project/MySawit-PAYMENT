package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PreviousWageConfigResponseTest {

    @Test
    void shouldBuildCorrectly() {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        PreviousWageConfigResponse response = PreviousWageConfigResponse.builder()
                .id(id)
                .upahBuruhPerKg(BigDecimal.valueOf(2.5))
                .upahSupirPerKg(BigDecimal.valueOf(1.5))
                .upahMandorPerKg(BigDecimal.valueOf(1.0))
                .deactivatedAt(now)
                .build();

        assertEquals(id, response.getId());
        assertEquals(BigDecimal.valueOf(2.5), response.getUpahBuruhPerKg());
        assertEquals(BigDecimal.valueOf(1.5), response.getUpahSupirPerKg());
        assertEquals(BigDecimal.valueOf(1.0), response.getUpahMandorPerKg());
        assertEquals(now, response.getDeactivatedAt());
    }
}