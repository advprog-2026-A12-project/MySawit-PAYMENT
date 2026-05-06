package id.ac.ui.cs.advprog.mysawitpayment.dto.response.internal;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class PayrollCreationResponse {
    private UUID payrollId;

    private boolean alreadyProcessed;
}
