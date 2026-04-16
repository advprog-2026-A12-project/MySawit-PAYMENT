package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class PreviousWageConfigResponse {

    private UUID id;
    private BigDecimal upahBuruhPerKg;
    private BigDecimal upahSupirPerKg;
    private BigDecimal upahMandorPerKg;
    private OffsetDateTime deactivatedAt;
}
