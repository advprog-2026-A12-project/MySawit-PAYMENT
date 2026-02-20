package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.dto.request.CreatePayrollRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollResponse;

import java.util.List;

public interface PayrollService {

    PayrollResponse createPayroll(CreatePayrollRequest request);

    List<PayrollResponse> getPayrollByUser(String userId);

    void approvePayroll(String payrollId);

    void rejectPayroll(String payrollId, String reason);
}