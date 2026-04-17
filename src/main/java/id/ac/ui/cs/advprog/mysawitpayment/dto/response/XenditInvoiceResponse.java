package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class XenditInvoiceResponse {
    private String id;

    @JsonProperty("external_id")
    private String externalId;

    @JsonProperty("invoice_url")
    private String invoiceUrl;

    private String status;

    @JsonProperty("expiry_date")
    private OffsetDateTime expiryDate;
}