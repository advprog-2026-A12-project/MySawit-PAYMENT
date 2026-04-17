package id.ac.ui.cs.advprog.mysawitpayment.controller;

import id.ac.ui.cs.advprog.mysawitpayment.dto.request.CreateTopUpRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.request.XenditCallbackRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.ApiResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.TopUpDetailResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.HistoryTopUpResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.CreateTopUpResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PageResponse;
import id.ac.ui.cs.advprog.mysawitpayment.service.TopUpService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/topup")
@RequiredArgsConstructor
public class TopUpController {

    private final TopUpService topUpService;

    @PostMapping
    public ApiResponse<CreateTopUpResponse> createTopUp(
            HttpServletRequest request,
            @RequestBody CreateTopUpRequest requestBody
    ) {
        validateAdmin(request);

        String adminIdStr = (String) request.getAttribute("userId");
        UUID adminId = UUID.fromString(adminIdStr);

        CreateTopUpResponse response = topUpService.createTopUp(requestBody, adminId);

        return ApiResponse.<CreateTopUpResponse>builder()
                .status("success")
                .message("Top-up created successfully")
                .data(response)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @GetMapping
    public ApiResponse<PageResponse<HistoryTopUpResponse>> getMyTopUps(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        validateAdmin(request);

        String adminIdStr = (String) request.getAttribute("userId");
        UUID adminId = UUID.fromString(adminIdStr);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Page<HistoryTopUpResponse> resultPage = topUpService.getMyTopUps(
                adminId,
                pageable
        );

        PageResponse<HistoryTopUpResponse> response = PageResponse.<HistoryTopUpResponse>builder()
                .content(resultPage.getContent())
                .page(resultPage.getNumber())
                .size(resultPage.getSize())
                .totalElements(resultPage.getTotalElements())
                .totalPages(resultPage.getTotalPages())
                .first(resultPage.isFirst())
                .last(resultPage.isLast())
                .build();

        return ApiResponse.<PageResponse<HistoryTopUpResponse>>builder()
                .status("success")
                .message("Top-up history retrieved successfully")
                .data(response)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @GetMapping("/{topupId:[0-9a-fA-F\\\\-]{36}}")
    public ApiResponse<TopUpDetailResponse> getTopUpDetail(
            HttpServletRequest request,
            @PathVariable UUID topupId
    ) {
        validateAdmin(request);

        String adminIdStr = (String) request.getAttribute("userId");
        UUID adminId = UUID.fromString(adminIdStr);

        TopUpDetailResponse response = topUpService.getTopUpDetail(topupId, adminId);

        return ApiResponse.<TopUpDetailResponse>builder()
                .status("success")
                .message("Top-up detail retrieved successfully")
                .data(response)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @PostMapping("/callback")
    public Map<String, String> handleXenditCallback(
            @RequestHeader(value = "x-callback-token", required = false) String callbackToken,
            @RequestBody XenditCallbackRequest request
    ) {
        topUpService.handleXenditCallback(callbackToken, request);
        return Map.of("status", "success");
    }

    private void validateAdmin(HttpServletRequest request) {
        String role = (String) request.getAttribute("userRole");
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("Forbidden");
        }
    }
}