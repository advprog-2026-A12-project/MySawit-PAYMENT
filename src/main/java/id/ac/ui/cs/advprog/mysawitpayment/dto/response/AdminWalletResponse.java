package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class AdminWalletResponse {

    private UUID id;

    private UUID userId;

    private BigDecimal balance;

    private String currency;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
