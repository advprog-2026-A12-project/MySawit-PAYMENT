package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import id.ac.ui.cs.advprog.mysawitpayment.model.enums.PaymentTransactionStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class TopUpDetailResponse {

    private UUID id;

    private AdminReferenceResponse admin;

    private BigDecimal amountSawitDollar;

    private BigDecimal amountIdr;

    private String exchangeRate;

    private String paymentGateway;

    private String gatewayReferenceId;

    private PaymentTransactionStatus status;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
