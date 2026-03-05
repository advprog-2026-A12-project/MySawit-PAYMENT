package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.model.Payroll;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.PayrollStatus;

import org.springframework.data.domain.Page;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface PayrollService {

    Page<Payroll> getMyPayrolls(
            UUID userId,
            PayrollStatus status,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            int page,
            int size
    );

    Payroll getPayrollById(UUID payrollId);

    Payroll approvePayroll(UUID payrollId, UUID adminId);

    Payroll rejectPayroll(UUID payrollId, UUID adminId, String reason);

    Payroll createPayroll(Payroll payroll);
}