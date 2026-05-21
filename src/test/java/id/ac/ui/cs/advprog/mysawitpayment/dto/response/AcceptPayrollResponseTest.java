package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AcceptPayrollResponseTest {

    @Test
    void testGetterAndSetter() {
        AcceptPayrollResponse response = new AcceptPayrollResponse();

        UUID id = UUID.randomUUID();

        PayrollUserResponse user = new PayrollUserResponse();
        user.setId(UUID.randomUUID());
        user.setRole("BURUH");

        BigDecimal amount = BigDecimal.valueOf(562.61);
        String status = "ACCEPTED";

        PayrollApprovedByResponse approvedBy = new PayrollApprovedByResponse();
        approvedBy.setId(UUID.randomUUID());

        OffsetDateTime approvedAt = OffsetDateTime.now(ZoneOffset.UTC);

        PayrollWalletResponse adminWallet = new PayrollWalletResponse();
        adminWallet.setBalanceBefore(BigDecimal.valueOf(50000.00));
        adminWallet.setBalanceAfter(BigDecimal.valueOf(49437.39));

        PayrollWalletResponse workerWallet = new PayrollWalletResponse();
        workerWallet.setBalanceBefore(BigDecimal.valueOf(688.14));
        workerWallet.setBalanceAfter(BigDecimal.valueOf(1250.75));

        PayrollDisbursementResponse disbursement = new PayrollDisbursementResponse();
        disbursement.setAdminWallet(adminWallet);
        disbursement.setWorkerWallet(workerWallet);

        response.setId(id);
        response.setUser(user);
        response.setAmount(amount);
        response.setStatus(status);
        response.setApprovedBy(approvedBy);
        response.setApprovedAt(approvedAt);
        response.setDisbursement(disbursement);

        assertEquals(id, response.getId());
        assertEquals(user, response.getUser());
        assertEquals(amount, response.getAmount());
        assertEquals(status, response.getStatus());
        assertEquals(approvedBy, response.getApprovedBy());
        assertEquals(approvedAt, response.getApprovedAt());
        assertEquals(disbursement, response.getDisbursement());
    }
}
