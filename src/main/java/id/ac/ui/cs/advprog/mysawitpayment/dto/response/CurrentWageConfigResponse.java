package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class CurrentWageConfigResponse {

    private UUID id;

    private BigDecimal upahBuruhPerKg;

    private BigDecimal upahSupirPerKg;

    private BigDecimal upahMandorPerKg;

    private String currency;

    private boolean isActive;

    private UpdatedByResponse updatedBy;

    private OffsetDateTime effectiveFrom;

    private OffsetDateTime createdAt;
}
