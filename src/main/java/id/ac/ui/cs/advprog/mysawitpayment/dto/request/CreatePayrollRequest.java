package id.ac.ui.cs.advprog.mysawitpayment.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePayrollRequest {

    private Long userId;

    private Double kilogram;

    private String referenceId;

    private String referenceType;
}