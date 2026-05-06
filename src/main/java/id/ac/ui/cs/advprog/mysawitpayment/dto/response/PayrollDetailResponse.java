package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class PayrollDetailResponse {

    private UUID id;

    private PayrollUserResponse user;

    private BigDecimal amount;

    private BigDecimal kilogram;

    private BigDecimal ratePerKg;

    private BigDecimal multiplier;

    private String status;

    private String description;

    private String rejectionReason;

    private String referenceType;

    private UUID referenceId;

    private PayrollApprovedByResponse approvedBy;

    private OffsetDateTime approvedAt;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
