package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.dto.request.CreatePayrollRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.model.Payroll;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.PayrollStatus;
import id.ac.ui.cs.advprog.mysawitpayment.repository.PayrollRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PayrollServiceImpl implements PayrollService {

    private final PayrollRepository payrollRepository;

    public PayrollServiceImpl(PayrollRepository payrollRepository) {
        this.payrollRepository = payrollRepository;
    }

    @Override
    public PayrollResponse createPayroll(CreatePayrollRequest request) {
        Payroll payroll = new Payroll();
        payroll.setUserId(request.getUserId());
        payroll.setKilogram(request.getKilogram());
        payroll.setReferenceId(request.getReferenceId());
        payroll.setReferenceType(request.getReferenceType());
        payroll.setAmount(request.getKilogram()); // dummy logic
        payroll.setStatus(PayrollStatus.PENDING);
        payroll.setCreatedAt(LocalDateTime.now());

        Payroll saved = payrollRepository.save(payroll);

        return mapToResponse(saved);
    }

    @Override
    public List<PayrollResponse> getPayrollByUser(Long userId) {
        return payrollRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public void approvePayroll(Long payrollId) {

        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new RuntimeException("Payroll not found"));

        if (payroll.getStatus() != PayrollStatus.PENDING) {
            throw new IllegalStateException("Already processed");
        }

        payroll.setStatus(PayrollStatus.ACCEPTED);

        payrollRepository.save(payroll);
    }

    @Override
    public void rejectPayroll(Long payrollId, String reason) {

        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new RuntimeException("Payroll not found"));

        if (payroll.getStatus() != PayrollStatus.PENDING) {
            throw new IllegalStateException("Already processed");
        }

        payroll.setStatus(PayrollStatus.REJECTED);

        payrollRepository.save(payroll);
    }

    private PayrollResponse mapToResponse(Payroll payroll) {
        PayrollResponse response = new PayrollResponse();
        response.setId(payroll.getId());
        response.setUserId(payroll.getUserId());
        response.setKilogram(payroll.getKilogram());
        response.setAmount(payroll.getAmount());
        response.setStatus(payroll.getStatus());
        response.setReferenceId(payroll.getReferenceId());
        response.setReferenceType(payroll.getReferenceType());
        response.setCreatedAt(payroll.getCreatedAt());
        return response;
    }

}
