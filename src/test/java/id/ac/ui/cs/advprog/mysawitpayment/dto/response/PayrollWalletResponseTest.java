package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PayrollWalletResponseTest {

    @Test
    void testGetterAndSetter() {
        PayrollWalletResponse response = new PayrollWalletResponse();

        BigDecimal balanceBefore = BigDecimal.valueOf(1000.00);
        BigDecimal balanceAfter = BigDecimal.valueOf(1500.00);

        response.setBalanceBefore(balanceBefore);
        response.setBalanceAfter(balanceAfter);

        assertEquals(balanceBefore, response.getBalanceBefore());
        assertEquals(balanceAfter, response.getBalanceAfter());
    }
}