package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class HistoryTopUpResponse {

    private UUID id;

    private BigDecimal amountSawitDollar;

    private BigDecimal amountIdr;

    private String paymentGateway;

    private String status;

    private String paymentUrl;

    private OffsetDateTime expiresAt;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
