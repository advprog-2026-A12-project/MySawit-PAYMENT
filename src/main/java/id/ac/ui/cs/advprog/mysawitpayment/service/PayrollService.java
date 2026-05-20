package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.dto.request.internal.PayrollCreationRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.AcceptPayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.AdminPayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollDetailResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.RejectPayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.internal.PayrollCreationResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.request.filter.PayrollFilter;
import id.ac.ui.cs.advprog.mysawitpayment.security.AuthenticatedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PayrollService {

    Page<AdminPayrollResponse> getAllPayrolls(AuthenticatedUser requester, PayrollFilter filter, Pageable pageable);

    Page<PayrollResponse> getMyPayrolls(AuthenticatedUser requester, PayrollFilter filter, Pageable pageable);

    PayrollDetailResponse getPayrollById(UUID payrollId, AuthenticatedUser requester);

    AcceptPayrollResponse acceptPayroll(UUID payrollId, AuthenticatedUser requester);

    RejectPayrollResponse rejectPayroll(UUID payrollId, AuthenticatedUser requester, String reason);

    PayrollCreationResponse createPayroll(PayrollCreationRequest request);
}
