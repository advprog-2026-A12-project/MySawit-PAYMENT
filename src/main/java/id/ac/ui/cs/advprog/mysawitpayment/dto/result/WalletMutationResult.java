package id.ac.ui.cs.advprog.mysawitpayment.dto.result;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Builder
@Getter
public class WalletMutationResult {
    private BigDecimal balanceBefore;

    private BigDecimal balanceAfter;
}
