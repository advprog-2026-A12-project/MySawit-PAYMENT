package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import id.ac.ui.cs.advprog.mysawitpayment.model.enums.PaymentTransactionStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class CreateTopUpResponse {

    private UUID id;

    private BigDecimal amountSawitDollar;

    private BigDecimal amountIdr;

    private String exchangeRate;

    private String paymentGateway;

    private PaymentTransactionStatus status;

    private String paymentUrl;

    private OffsetDateTime expiresAt;

    private OffsetDateTime createdAt;
}
