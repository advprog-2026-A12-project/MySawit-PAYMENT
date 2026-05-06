package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.dto.response.AcceptPayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.RejectPayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollDetailResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.AdminPayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollDisbursementResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollUserResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollApprovedByResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollWalletResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.result.WalletMutationResult;
import id.ac.ui.cs.advprog.mysawitpayment.exception.PayrollAlreadyProcessedException;
import id.ac.ui.cs.advprog.mysawitpayment.exception.PayrollNotFoundException;
import id.ac.ui.cs.advprog.mysawitpayment.model.Payroll;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.PayrollStatus;
import id.ac.ui.cs.advprog.mysawitpayment.repository.PayrollRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PayrollServiceImpl implements PayrollService {

    private final PayrollRepository payrollRepository;
    private final WalletService walletService;

    @Override
    public Page<AdminPayrollResponse> getAllPayrolls(Pageable pageable) {
        Page<Payroll> allPayrolls = getPayrollsInternal(null, pageable);
        return allPayrolls.map(this::mapToAdminResponse);
    }

    @Override
    public Page<PayrollResponse> getMyPayrolls(UUID userId, Pageable pageable) {
        Page<Payroll> myPayrolls = getPayrollsInternal(userId, pageable);
        return myPayrolls.map(this::mapToResponse);
    }

    private Page<Payroll> getPayrollsInternal(UUID userId, Pageable pageable) {
        if (userId != null) {
            return payrollRepository.findByUserId(userId, pageable);
        } else {
            return payrollRepository.findAll(pageable);
        }
    }

    @Override
    public PayrollDetailResponse getPayrollById(UUID payrollId) {
        return mapToDetailResponse(findPayrollOrThrow(payrollId));
    }

    @Override
    @Transactional
    public AcceptPayrollResponse acceptPayroll(UUID payrollId, UUID adminId) {

        Payroll payroll = findPayrollOrThrow(payrollId);
        ensurePending(payroll, payrollId);

        WalletMutationResult adminWalletResult = walletService.debitWallet(
                adminId,
                payroll.getAmount(),
                "PAYROLL_DEDUCTION",
                payroll.getId(),
                "Pengurangan saldo untuk pembayaran payroll"
        );

        WalletMutationResult workerWalletResult = walletService.creditWallet(
                payroll.getUserId(),
                payroll.getAmount(),
                "PAYROLL_DISBURSEMENT",
                payroll.getId(),
                "Pencairan payroll"
        );

        payroll.setStatus(PayrollStatus.ACCEPTED);
        payroll.setApprovedBy(adminId);
        payroll.setApprovedAt(OffsetDateTime.now());

        Payroll savedPayroll = payrollRepository.save(payroll);

        return mapToAcceptResponse(
                savedPayroll,
                adminWalletResult,
                workerWalletResult
        );
    }

    @Override
    public RejectPayrollResponse rejectPayroll(UUID payrollId, UUID adminId, String reason) {

        Payroll payroll = findPayrollOrThrow(payrollId);
        ensurePending(payroll, payrollId);

        payroll.setStatus(PayrollStatus.REJECTED);
        payroll.setApprovedBy(adminId);
        payroll.setApprovedAt(OffsetDateTime.now());
        payroll.setRejectionReason(reason);

        return mapToRejectResponse(payrollRepository.save(payroll));
    }

    @Override
    public PayrollResponse createPayroll(Payroll payroll) {
        return mapToResponse(payrollRepository.save(payroll));
    }

    private Payroll findPayrollOrThrow(UUID payrollId) {
        return payrollRepository.findById(payrollId)
                .orElseThrow(() ->
                        new PayrollNotFoundException("Payroll " + payrollId + " not found"));
    }

    private void ensurePending(Payroll payroll, UUID payrollId) {
        if (!PayrollStatus.PENDING.equals(payroll.getStatus())) {
            throw new PayrollAlreadyProcessedException(
                    "Payroll " + payrollId + " already processed");
        }
    }

    private PayrollResponse mapToResponse(Payroll payroll) {

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

    private AdminPayrollResponse mapToAdminResponse(Payroll payroll) {

        AdminPayrollResponse response = new AdminPayrollResponse();

        PayrollUserResponse user = new PayrollUserResponse();

        user.setId(payroll.getUserId());


        response.setId(payroll.getId());
        response.setUser(user);
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

    private PayrollDetailResponse mapToDetailResponse(Payroll payroll) {
        PayrollUserResponse user = new PayrollUserResponse();

        user.setId(payroll.getUserId());

        PayrollApprovedByResponse approvedBy = new PayrollApprovedByResponse();

        approvedBy.setId(payroll.getApprovedBy());

        PayrollDetailResponse response = new PayrollDetailResponse();

        response.setId(payroll.getId());
        response.setUser(user);
        response.setAmount(payroll.getAmount());
        response.setKilogram(payroll.getKilogram());
        response.setRatePerKg(payroll.getRatePerKg());
        response.setMultiplier(payroll.getMultiplier());
        response.setStatus(payroll.getStatus().name());
        response.setDescription(payroll.getDescription());
        response.setRejectionReason(payroll.getRejectionReason());
        response.setReferenceType(payroll.getReferenceType().name());
        response.setReferenceId(payroll.getReferenceId());
        response.setApprovedBy(approvedBy);
        response.setApprovedAt(payroll.getApprovedAt());
        response.setCreatedAt(payroll.getCreatedAt());
        response.setUpdatedAt(payroll.getUpdatedAt());

        return response;
    }

    private AcceptPayrollResponse mapToAcceptResponse(
            Payroll payroll,
            WalletMutationResult adminWalletResult,
            WalletMutationResult workerWalletResult
    ) {
        PayrollUserResponse user = new PayrollUserResponse();
        user.setId(payroll.getUserId());
        //TODO: ambil dari auth
        user.setName("");
        user.setRole("");

        PayrollApprovedByResponse approvedBy = new PayrollApprovedByResponse();
        approvedBy.setId(payroll.getApprovedBy());
        // TODO: ambil dari auth
        approvedBy.setName("");

        PayrollWalletResponse adminWallet = new PayrollWalletResponse();
        adminWallet.setBalanceBefore(adminWalletResult.getBalanceBefore());
        adminWallet.setBalanceAfter(adminWalletResult.getBalanceAfter());

        PayrollWalletResponse workerWallet = new PayrollWalletResponse();
        workerWallet.setBalanceBefore(workerWalletResult.getBalanceBefore());
        workerWallet.setBalanceAfter(workerWalletResult.getBalanceAfter());

        PayrollDisbursementResponse disbursement = new PayrollDisbursementResponse();
        disbursement.setAdminWallet(adminWallet);
        disbursement.setWorkerWallet(workerWallet);

        AcceptPayrollResponse response = new AcceptPayrollResponse();
        response.setId(payroll.getId());
        response.setUser(user);
        response.setAmount(payroll.getAmount());
        response.setStatus(payroll.getStatus().name());
        response.setApprovedBy(approvedBy);
        response.setApprovedAt(payroll.getApprovedAt());
        response.setDisbursement(disbursement);

        return response;
    }

    private RejectPayrollResponse mapToRejectResponse(Payroll payroll) {
        PayrollUserResponse user = new PayrollUserResponse();
        user.setId(payroll.getUserId());
        //TODO: ambil dari auth
        user.setName("");
        user.setRole("");

        PayrollApprovedByResponse approvedBy = new PayrollApprovedByResponse();
        approvedBy.setId(payroll.getApprovedBy());
        // TODO: ambil dari auth
        approvedBy.setName("");

        RejectPayrollResponse response = new RejectPayrollResponse();

        response.setId(payroll.getId());
        response.setUser(user);
        response.setAmount(payroll.getAmount());
        response.setStatus(payroll.getStatus().name());
        response.setRejectionReason(payroll.getRejectionReason());
        response.setApprovedBy(approvedBy);
        response.setApprovedAt(payroll.getApprovedAt());

        return response;
    }
}