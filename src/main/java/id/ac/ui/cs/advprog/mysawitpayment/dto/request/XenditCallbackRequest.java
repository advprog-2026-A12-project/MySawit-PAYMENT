package id.ac.ui.cs.advprog.mysawitpayment.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
public class XenditCallbackRequest {
    @NotBlank(message = "Xendit callback id is required")
    private String id;

    @NotBlank(message = "External id is required")
    @JsonProperty("external_id")
    private String externalId;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "PAID|EXPIRED|FAILED", message = "Unsupported Xendit callback status")
    private String status;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @JsonProperty("paid_at")
    private OffsetDateTime paidAt;
}
