package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.exception.PayrollNotFoundException;
import id.ac.ui.cs.advprog.mysawitpayment.model.Payroll;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.PayrollStatus;
import id.ac.ui.cs.advprog.mysawitpayment.repository.PayrollRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PayrollServiceImpl implements PayrollService {

    private final PayrollRepository payrollRepository;

    @Override
    public Payroll createPayroll(Payroll payroll) {
        return payrollRepository.save(payroll);
    }

    @Override
    public List<Payroll> getAllPayrolls() {
        return payrollRepository.findAll();
    }

    @Override
    public List<Payroll> getMyPayrolls(UUID userId) {
        return payrollRepository.findByUserId(userId);
    }

    @Override
    public Payroll getPayrollById(UUID payrollId) {
        return payrollRepository.findById(payrollId)
                .orElseThrow(() ->
                        new PayrollNotFoundException("Payroll " + payrollId + " not found")
                );
    }

    @Override
    public Payroll acceptPayroll(UUID payrollId, UUID adminId) {

        Payroll payroll = getPayrollById(payrollId);

        if (payroll.getStatus() != PayrollStatus.PENDING) {
            throw new IllegalStateException("Payroll is not in PENDING status");
        }

        payroll.setStatus(PayrollStatus.ACCEPTED);
        payroll.setApprovedBy(adminId);
        payroll.setApprovedAt(OffsetDateTime.now());

        return payrollRepository.save(payroll);
    }

    @Override
    public Payroll rejectPayroll(UUID payrollId, UUID adminId, String rejectionReason) {

        Payroll payroll = getPayrollById(payrollId);

        if (payroll.getStatus() != PayrollStatus.PENDING) {
            throw new IllegalStateException("Payroll is not in PENDING status");
        }

        payroll.setStatus(PayrollStatus.REJECTED);
        payroll.setRejectionReason(rejectionReason);
        payroll.setApprovedBy(adminId);
        payroll.setApprovedAt(OffsetDateTime.now());

        return payrollRepository.save(payroll);
    }
}