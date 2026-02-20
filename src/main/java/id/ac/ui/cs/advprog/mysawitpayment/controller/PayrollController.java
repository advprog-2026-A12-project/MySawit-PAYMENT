package id.ac.ui.cs.advprog.mysawitpayment.controller;

import id.ac.ui.cs.advprog.mysawitpayment.dto.request.CreatePayrollRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.service.PayrollService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payrolls")
public class PayrollController {

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @PostMapping
    public ResponseEntity<PayrollResponse> create(@RequestBody CreatePayrollRequest request) {
        return ResponseEntity.ok(payrollService.createPayroll(request));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PayrollResponse>> getByUser(@PathVariable String userId) {
        return ResponseEntity.ok(payrollService.getPayrollByUser(userId));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable String id) {
        payrollService.approvePayroll(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Void> reject(@PathVariable String id, @RequestParam String reason) {
        payrollService.rejectPayroll(id, reason);
        return ResponseEntity.ok().build();
    }
}