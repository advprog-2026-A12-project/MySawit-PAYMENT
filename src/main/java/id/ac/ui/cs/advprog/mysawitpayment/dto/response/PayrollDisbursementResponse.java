package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayrollDisbursementResponse {
    private PayrollWalletResponse adminWallet;

    private PayrollWalletResponse workerWallet;
}
