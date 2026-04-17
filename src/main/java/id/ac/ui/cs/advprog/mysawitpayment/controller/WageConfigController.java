package id.ac.ui.cs.advprog.mysawitpayment.controller;

import id.ac.ui.cs.advprog.mysawitpayment.dto.request.CreateWageConfigRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.ApiResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.CreateWageConfigResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.CurrentWageConfigResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.HistoryWageConfigResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PageResponse;
import id.ac.ui.cs.advprog.mysawitpayment.service.WageConfigService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wage-configs")
@RequiredArgsConstructor
public class WageConfigController {

    private final WageConfigService wageConfigService;

    @GetMapping("/active")
    public ApiResponse<CurrentWageConfigResponse> getActiveWageConfig(HttpServletRequest request) {
        validateAdmin(request);

        CurrentWageConfigResponse response = wageConfigService.getCurrentWageConfig();

        return ApiResponse.<CurrentWageConfigResponse>builder()
                .status("success")
                .message("Active wage config retrieved successfully")
                .data(response)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @PostMapping
    public ApiResponse<CreateWageConfigResponse> createNewWageConfig(
            HttpServletRequest request,
            @Valid @RequestBody CreateWageConfigRequest createWageConfigRequest
    ) {
        validateAdmin(request);

        String userIdStr = (String) request.getAttribute("userId");
        UUID adminId = UUID.fromString(userIdStr);

        CreateWageConfigResponse response = wageConfigService.createWageConfig(createWageConfigRequest, adminId);

        return ApiResponse.<CreateWageConfigResponse>builder()
                .status("success")
                .message("Wage config updated successfully")
                .data(response)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @GetMapping("/history")
    public ApiResponse<PageResponse<HistoryWageConfigResponse>> getWageConfigHistory(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        validateAdmin(request);

        Page<HistoryWageConfigResponse> wageConfigPage =
                wageConfigService.getWageConfigHistory(PageRequest.of(page, size));

        PageResponse<HistoryWageConfigResponse> pageResponse = PageResponse.<HistoryWageConfigResponse>builder()
                .content(wageConfigPage.getContent())
                .page(wageConfigPage.getNumber())
                .size(wageConfigPage.getSize())
                .totalElements(wageConfigPage.getTotalElements())
                .totalPages(wageConfigPage.getTotalPages())
                .first(wageConfigPage.isFirst())
                .last(wageConfigPage.isLast())
                .build();

        return ApiResponse.<PageResponse<HistoryWageConfigResponse>>builder()
                .status("success")
                .message("Wage config history retrieved successfully")
                .data(pageResponse)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    private void validateAdmin(HttpServletRequest request) {
        String role = (String) request.getAttribute("userRole");
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("Forbidden");
        }
    }
}