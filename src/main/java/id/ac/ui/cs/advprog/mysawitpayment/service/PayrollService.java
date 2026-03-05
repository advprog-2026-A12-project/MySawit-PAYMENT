package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.model.Payroll;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.PayrollStatus;

import org.springframework.data.domain.Page;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface PayrollService {

    Page<PayrollResponse> getMyPayrolls(
            UUID userId,
            PayrollStatus status,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            int page,
            int size
    );

    PayrollResponse getPayrollById(UUID payrollId);

    PayrollResponse approvePayroll(UUID payrollId, UUID adminId);

    PayrollResponse rejectPayroll(UUID payrollId, UUID adminId, String reason);

    PayrollResponse createPayroll(Payroll payroll);
}