package id.ac.ui.cs.advprog.mysawitpayment.dto.result;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class CreateInvoiceResult {

    private String gatewayReferenceId;

    private String paymentUrl;

    private OffsetDateTime expiresAt;

    private String status;
}