package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.dto.response.AcceptPayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.AdminPayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollDetailResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.RejectPayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.model.Payroll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PayrollService {

    Page<AdminPayrollResponse> getAllPayrolls(Pageable pageable);

    Page<PayrollResponse> getMyPayrolls(UUID userId, Pageable pageable);

    PayrollDetailResponse getPayrollById(UUID payrollId);

    AcceptPayrollResponse acceptPayroll(UUID payrollId, UUID adminId);

    RejectPayrollResponse rejectPayroll(UUID payrollId, UUID adminId, String reason);

    PayrollResponse createPayroll(Payroll payroll);
}