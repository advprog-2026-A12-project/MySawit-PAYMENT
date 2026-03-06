package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class PayrollResponse {

    private UUID id;

    private BigDecimal amount;

    private BigDecimal kilogram;

    private BigDecimal ratePerKg;

    private BigDecimal multiplier;

    private String status;

    private String referenceType;

    private String description;

    private OffsetDateTime approvedAt;

    private OffsetDateTime createdAt;
}