package id.ac.ui.cs.advprog.mysawitpayment.controller;

import id.ac.ui.cs.advprog.mysawitpayment.dto.request.RejectPayrollRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.request.filter.PayrollFilter;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.AcceptPayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.AdminPayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.ApiResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PageResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PayrollDetailResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.RejectPayrollResponse;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.PayrollStatus;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.ReferenceType;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.UserRole;
import id.ac.ui.cs.advprog.mysawitpayment.security.AuthenticatedUser;
import id.ac.ui.cs.advprog.mysawitpayment.service.PayrollService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payrolls")
@RequiredArgsConstructor
public class PayrollController {

    private static final Map<String, String> PAYROLL_ADMIN_SORT_FIELDS = Map.of(
            "createdAt", "createdAt",
            "amount", "amount",
            "kilogram", "kilogram"
    );

    private static final Map<String, String> PAYROLL_SELF_SORT_FIELDS = Map.of(
            "createdAt", "createdAt",
            "amount", "amount"
    );

    private final PayrollService payrollService;

    @GetMapping
    public ApiResponse<PageResponse<AdminPayrollResponse>> getPayrolls(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) PayrollStatus status,
            @RequestParam(required = false) UserRole userRole,
            @RequestParam(required = false) ReferenceType referenceType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        PageableRequest.validateDateRange(dateFrom, dateTo);

        Pageable pageable = PageableRequest.of(
                page,
                size,
                sort,
                PAYROLL_ADMIN_SORT_FIELDS,
                "createdAt,desc"
        );

        PayrollFilter filter = new PayrollFilter(
                userId,
                status,
                userRole,
                referenceType,
                PageableRequest.startOfDay(dateFrom),
                PageableRequest.startOfNextDay(dateTo)
        );

        Page<AdminPayrollResponse> payrollPage =
                payrollService.getAllPayrolls(AuthenticatedUser.from(request), filter, pageable);

        PageResponse<AdminPayrollResponse> pageResponse =
                PageResponse.<AdminPayrollResponse>builder()
                        .content(payrollPage.getContent())
                        .page(payrollPage.getNumber())
                        .size(payrollPage.getSize())
                        .totalElements(payrollPage.getTotalElements())
                        .totalPages(payrollPage.getTotalPages())
                        .first(payrollPage.isFirst())
                        .last(payrollPage.isLast())
                        .build();

        return ApiResponse.<PageResponse<AdminPayrollResponse>>builder()
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
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) PayrollStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        PageableRequest.validateDateRange(dateFrom, dateTo);

        Pageable pageable = PageableRequest.of(
                page,
                size,
                sort,
                PAYROLL_SELF_SORT_FIELDS,
                "createdAt,desc"
        );

        PayrollFilter filter = new PayrollFilter(
                null,
                status,
                null,
                null,
                PageableRequest.startOfDay(dateFrom),
                PageableRequest.startOfNextDay(dateTo)
        );

        Page<PayrollResponse> payrollPage =
                payrollService.getMyPayrolls(AuthenticatedUser.from(request), filter, pageable);

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
    public ApiResponse<PayrollDetailResponse> getPayrollById(
            HttpServletRequest request,
            @PathVariable UUID payrollId
    ) {

        PayrollDetailResponse response = payrollService.getPayrollById(
                payrollId,
                AuthenticatedUser.from(request)
        );

        return ApiResponse.<PayrollDetailResponse>builder()
                .status("success")
                .message("Payroll detail retrieved successfully")
                .data(response)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @PutMapping("/{payrollId}/accept")
    public ApiResponse<AcceptPayrollResponse> acceptPayroll(
            HttpServletRequest request,
            @PathVariable UUID payrollId
    ) {

        AcceptPayrollResponse response =
                payrollService.acceptPayroll(payrollId, AuthenticatedUser.from(request));

        return ApiResponse.<AcceptPayrollResponse>builder()
                .status("success")
                .message("Payroll accepted and disbursed successfully")
                .data(response)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @PutMapping("/{payrollId}/reject")
    public ApiResponse<RejectPayrollResponse> rejectPayroll(
            HttpServletRequest request,
            @PathVariable UUID payrollId,
            @Valid @RequestBody RejectPayrollRequest requestBody
    ) {

        RejectPayrollResponse response =
                payrollService.rejectPayroll(
                        payrollId,
                        AuthenticatedUser.from(request),
                        requestBody.getRejectionReason()
                );

        return ApiResponse.<RejectPayrollResponse>builder()
                .status("success")
                .message("Payroll rejected")
                .data(response)
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
