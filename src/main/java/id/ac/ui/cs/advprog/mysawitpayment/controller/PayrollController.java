package id.ac.ui.cs.advprog.mysawitpayment.controller;

import id.ac.ui.cs.advprog.mysawitpayment.dto.request.RejectPayrollRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.ApiResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PageResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.service.PayrollService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;


import jakarta.servlet.http.HttpServletRequest;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payrolls")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    @GetMapping
    public ApiResponse<PageResponse<PayrollResponse>> getPayrolls(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        String role = (String) request.getAttribute("userRole");

        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("Forbidden");
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Page<PayrollResponse> payrollPage =
                payrollService.getAllPayrolls(pageable);

        PageResponse<PayrollResponse> pageResponse =
                PageResponse.<PayrollResponse>builder()
                        .content(payrollPage.getContent())
                        .page(payrollPage.getNumber())
                        .size(payrollPage.getSize())
                        .totalElements(payrollPage.getTotalElements())
                        .totalPages(payrollPage.getTotalPages())
                        .first(payrollPage.isFirst())
                        .last(payrollPage.isLast())
                        .build();

        return ApiResponse.<PageResponse<PayrollResponse>>builder()
                .status("success")
                .message("Payrolls retrieved successfully")
                .data(pageResponse)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @GetMapping("/me")
    public ApiResponse<PageResponse<PayrollResponse>> getMyPayrolls(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        String userIdStr = (String) request.getAttribute("userId");
        UUID userId = UUID.fromString(userIdStr);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Page<PayrollResponse> payrollPage =
                payrollService.getMyPayrolls(userId, pageable);

        PageResponse<PayrollResponse> pageResponse =
                PageResponse.<PayrollResponse>builder()
                        .content(payrollPage.getContent())
                        .page(payrollPage.getNumber())
                        .size(payrollPage.getSize())
                        .totalElements(payrollPage.getTotalElements())
                        .totalPages(payrollPage.getTotalPages())
                        .first(payrollPage.isFirst())
                        .last(payrollPage.isLast())
                        .build();

        return ApiResponse.<PageResponse<PayrollResponse>>builder()
                .status("success")
                .message("My payrolls retrieved successfully")
                .data(pageResponse)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @GetMapping("/{payrollId:[0-9a-fA-F\\\\-]{36}}")
    public ApiResponse<PayrollResponse> getPayrollById(
            @PathVariable UUID payrollId
    ) {

        PayrollResponse response = payrollService.getPayrollById(payrollId);

        return ApiResponse.<PayrollResponse>builder()
                .status("success")
                .message("Payroll detail retrieved successfully")
                .data(response)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @PutMapping("/{payrollId}/accept")
    public ApiResponse<PayrollResponse> acceptPayroll(
            HttpServletRequest request,
            @PathVariable UUID payrollId
    ) {

        String adminIdStr = (String) request.getAttribute("userId");
        UUID adminId = UUID.fromString(adminIdStr);

        PayrollResponse response =
                payrollService.approvePayroll(payrollId, adminId);

        return ApiResponse.<PayrollResponse>builder()
                .status("success")
                .message("Payroll accepted and disbursed successfully")
                .data(response)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @PutMapping("/{payrollId}/reject")
    public ApiResponse<PayrollResponse> rejectPayroll(
            HttpServletRequest request,
            @PathVariable UUID payrollId,
            @RequestBody RejectPayrollRequest requestBody
    ) {

        String adminIdStr = (String) request.getAttribute("userId");
        UUID adminId = UUID.fromString(adminIdStr);

        PayrollResponse response =
                payrollService.rejectPayroll(payrollId, adminId, requestBody.getRejectionReason());

        return ApiResponse.<PayrollResponse>builder()
                .status("success")
                .message("Payroll rejected")
                .data(response)
                .timestamp(OffsetDateTime.now())
                .build();
    }
}