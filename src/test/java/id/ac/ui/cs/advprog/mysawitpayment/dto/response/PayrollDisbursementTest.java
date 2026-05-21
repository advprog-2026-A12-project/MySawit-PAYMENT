package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class PayrollDisbursementTest {
    @Test
    void testGetterAndSetter() {
        PayrollDisbursementResponse response = new PayrollDisbursementResponse();

        PayrollWalletResponse adminWallet = new PayrollWalletResponse();
        adminWallet.setBalanceBefore(BigDecimal.valueOf(50000.00));
        adminWallet.setBalanceAfter(BigDecimal.valueOf(49437.39));

        PayrollWalletResponse workerWallet = new PayrollWalletResponse();
        workerWallet.setBalanceBefore(BigDecimal.valueOf(688.14));
        workerWallet.setBalanceAfter(BigDecimal.valueOf(1250.75));

        response.setAdminWallet(adminWallet);
        response.setWorkerWallet(workerWallet);

        assertEquals(adminWallet, response.getAdminWallet());
        assertEquals(workerWallet, response.getWorkerWallet());
    }
}
