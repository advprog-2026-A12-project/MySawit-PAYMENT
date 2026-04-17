package id.ac.ui.cs.advprog.mysawitpayment.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
public class XenditCallbackRequest {
    private String id;

    @JsonProperty("external_id")
    private String externalId;

    private String status;

    private BigDecimal amount;

    @JsonProperty("paid_at")
    private OffsetDateTime paidAt;
}