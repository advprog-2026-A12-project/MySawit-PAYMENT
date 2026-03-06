package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.exception.PayrollAlreadyProcessedException;
import id.ac.ui.cs.advprog.mysawitpayment.exception.PayrollNotFoundException;
import id.ac.ui.cs.advprog.mysawitpayment.model.Payroll;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.PayrollStatus;
import id.ac.ui.cs.advprog.mysawitpayment.repository.PayrollRepository;
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

    @Override
    public Page<PayrollResponse> getAllPayrolls(Pageable pageable) {
        return getPayrollsInternal(null, pageable);
    }

    @Override
    public Page<PayrollResponse> getMyPayrolls(UUID userId, Pageable pageable) {
        return getPayrollsInternal(userId, pageable);
    }

    private Page<PayrollResponse> getPayrollsInternal(UUID userId, Pageable pageable) {

        Page<Payroll> payrollPage;

        if (userId != null) {
            payrollPage = payrollRepository.findByUserId(userId, pageable);
        } else {
            payrollPage = payrollRepository.findAll(pageable);
        }

        return payrollPage.map(this::mapToResponse);
    }

    @Override
    public PayrollResponse getPayrollById(UUID payrollId) {
        return mapToResponse(findPayrollOrThrow(payrollId));
    }

    @Override
    public PayrollResponse approvePayroll(UUID payrollId, UUID adminId) {

        Payroll payroll = findPayrollOrThrow(payrollId);
        ensurePending(payroll, payrollId);

        payroll.setStatus(PayrollStatus.ACCEPTED);
        payroll.setApprovedBy(adminId);
        payroll.setApprovedAt(OffsetDateTime.now());

        return mapToResponse(payrollRepository.save(payroll));
    }

    @Override
    public PayrollResponse rejectPayroll(UUID payrollId, UUID adminId, String reason) {

        Payroll payroll = findPayrollOrThrow(payrollId);
        ensurePending(payroll, payrollId);

        payroll.setStatus(PayrollStatus.REJECTED);
        payroll.setApprovedBy(adminId);
        payroll.setApprovedAt(OffsetDateTime.now());
        payroll.setRejectionReason(reason);

        return mapToResponse(payrollRepository.save(payroll));
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
}