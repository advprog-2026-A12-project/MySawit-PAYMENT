package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.model.Payroll;

import java.util.List;
import java.util.UUID;

public interface PayrollService {

    Payroll createPayroll(Payroll payroll);

    List<Payroll> getAllPayrolls();

    List<Payroll> getMyPayrolls(UUID userId);

    Payroll getPayrollById(UUID payrollId);

    Payroll acceptPayroll(UUID payrollId, UUID adminId);

    Payroll rejectPayroll(UUID payrollId, UUID adminId, String rejectionReason);
}