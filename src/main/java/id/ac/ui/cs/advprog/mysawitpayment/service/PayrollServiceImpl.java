package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.dto.request.CreatePayrollRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.repository.PayrollRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PayrollServiceImpl implements PayrollService {

    private final PayrollRepository payrollRepository;

    public PayrollServiceImpl(PayrollRepository payrollRepository) {
        this.payrollRepository = payrollRepository;
    }

    @Override
    public PayrollResponse createPayroll(CreatePayrollRequest request) {
        return null;
    }

    @Override
    public List<PayrollResponse> getPayrollByUser(String userId) {
        return null;
    }

    @Override
    public void approvePayroll(String payrollId) {
    }

    @Override
    public void rejectPayroll(String payrollId, String reason) {
    }

}
