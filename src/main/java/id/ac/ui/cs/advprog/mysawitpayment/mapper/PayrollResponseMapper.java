package id.ac.ui.cs.advprog.mysawitpayment.mapper;

import id.ac.ui.cs.advprog.mysawitpayment.dto.response.AcceptPayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.AdminPayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollApprovedByResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollDetailResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollDisbursementResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollUserResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollWalletResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.RejectPayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.result.WalletMutationResult;
import id.ac.ui.cs.advprog.mysawitpayment.model.Payroll;
import org.springframework.stereotype.Component;

@Component
public class PayrollResponseMapper {

    public PayrollResponse toPayrollResponse(Payroll payroll) {
        PayrollResponse response = new PayrollResponse();

        response.setId(payroll.getId());
        response.setAmount(payroll.getAmount());
        response.setKilogram(payroll.getKilogram());
        response.setRatePerKg(payroll.getRatePerKg());
        response.setMultiplier(payroll.getMultiplier());
        response.setStatus(payroll.getStatus().name());
        response.setReferenceType(payroll.getReferenceType().name());
        response.setDescription(payroll.getDescription());
        response.setApprovedAt(payroll.getApprovedAt());
        response.setCreatedAt(payroll.getCreatedAt());

        return response;
    }

    public AdminPayrollResponse toAdminPayrollResponse(Payroll payroll) {
        AdminPayrollResponse response = new AdminPayrollResponse();

        response.setId(payroll.getId());
        response.setUser(toUserResponse(payroll));
        response.setAmount(payroll.getAmount());
        response.setKilogram(payroll.getKilogram());
        response.setRatePerKg(payroll.getRatePerKg());
        response.setMultiplier(payroll.getMultiplier());
        response.setStatus(payroll.getStatus().name());
        response.setReferenceType(payroll.getReferenceType().name());
        response.setReferenceId(payroll.getReferenceId());
        response.setDescription(payroll.getDescription());
        response.setCreatedAt(payroll.getCreatedAt());

        return response;
    }

    public PayrollDetailResponse toPayrollDetailResponse(Payroll payroll) {
        PayrollDetailResponse response = new PayrollDetailResponse();

        response.setId(payroll.getId());
        response.setUser(toUserResponse(payroll));
        response.setAmount(payroll.getAmount());
        response.setKilogram(payroll.getKilogram());
        response.setRatePerKg(payroll.getRatePerKg());
        response.setMultiplier(payroll.getMultiplier());
        response.setStatus(payroll.getStatus().name());
        response.setDescription(payroll.getDescription());
        response.setRejectionReason(payroll.getRejectionReason());
        response.setReferenceType(payroll.getReferenceType().name());
        response.setReferenceId(payroll.getReferenceId());
        response.setApprovedBy(toApprovedByResponse(payroll));
        response.setApprovedAt(payroll.getApprovedAt());
        response.setCreatedAt(payroll.getCreatedAt());
        response.setUpdatedAt(payroll.getUpdatedAt());

        return response;
    }

    public AcceptPayrollResponse toAcceptPayrollResponse(
            Payroll payroll,
            WalletMutationResult adminWalletResult,
            WalletMutationResult workerWalletResult
    ) {
        PayrollDisbursementResponse disbursement = new PayrollDisbursementResponse();
        disbursement.setAdminWallet(toWalletResponse(adminWalletResult));
        disbursement.setWorkerWallet(toWalletResponse(workerWalletResult));

        AcceptPayrollResponse response = new AcceptPayrollResponse();
        response.setId(payroll.getId());
        response.setUser(toUserResponse(payroll));
        response.setAmount(payroll.getAmount());
        response.setStatus(payroll.getStatus().name());
        response.setApprovedBy(toApprovedByResponse(payroll));
        response.setApprovedAt(payroll.getApprovedAt());
        response.setDisbursement(disbursement);

        return response;
    }

    public RejectPayrollResponse toRejectPayrollResponse(Payroll payroll) {
        RejectPayrollResponse response = new RejectPayrollResponse();

        response.setId(payroll.getId());
        response.setUser(toUserResponse(payroll));
        response.setAmount(payroll.getAmount());
        response.setStatus(payroll.getStatus().name());
        response.setRejectionReason(payroll.getRejectionReason());
        response.setApprovedBy(toApprovedByResponse(payroll));
        response.setApprovedAt(payroll.getApprovedAt());

        return response;
    }

    private PayrollUserResponse toUserResponse(Payroll payroll) {
        PayrollUserResponse user = new PayrollUserResponse();
        user.setId(payroll.getUserId());
        user.setRole(payroll.getUserRole().name());
        return user;
    }

    private PayrollApprovedByResponse toApprovedByResponse(Payroll payroll) {
        PayrollApprovedByResponse approvedBy = new PayrollApprovedByResponse();
        approvedBy.setId(payroll.getApprovedBy());
        return approvedBy;
    }

    private PayrollWalletResponse toWalletResponse(WalletMutationResult walletResult) {
        PayrollWalletResponse wallet = new PayrollWalletResponse();
        wallet.setBalanceBefore(walletResult.getBalanceBefore());
        wallet.setBalanceAfter(walletResult.getBalanceAfter());
        return wallet;
    }
}
