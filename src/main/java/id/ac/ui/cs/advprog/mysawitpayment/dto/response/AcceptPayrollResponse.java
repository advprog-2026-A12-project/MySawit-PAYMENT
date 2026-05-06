package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class AcceptPayrollResponse {

    private UUID id;

    private PayrollUserResponse user;

    private BigDecimal amount;

    private String status;

    private PayrollApprovedByResponse approvedBy;

    private OffsetDateTime approvedAt;

    private PayrollDisbursementResponse disbursement;
}
