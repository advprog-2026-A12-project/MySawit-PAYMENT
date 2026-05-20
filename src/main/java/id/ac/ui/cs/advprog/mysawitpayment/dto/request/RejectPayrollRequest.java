package id.ac.ui.cs.advprog.mysawitpayment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectPayrollRequest {

    @NotBlank
    @Size(min = 10)
    private String rejectionReason;

}
