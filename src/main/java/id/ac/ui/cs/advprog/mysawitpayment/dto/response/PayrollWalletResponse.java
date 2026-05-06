package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PayrollWalletResponse {
    private BigDecimal balanceBefore;

    private BigDecimal balanceAfter;
}
