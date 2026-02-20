package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import id.ac.ui.cs.advprog.mysawitpayment.model.enums.PayrollStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PayrollResponse {

    private Long id;

    private Long userId;

    private Double kilogram;

    private Double amount;

    private PayrollStatus status;

    private String referenceId;

    private String referenceType;

    private LocalDateTime createdAt;
}