package id.ac.ui.cs.advprog.mysawitpayment.controller;


import id.ac.ui.cs.advprog.mysawitpayment.dto.response.*;
import id.ac.ui.cs.advprog.mysawitpayment.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/me")
    public ApiResponse<WalletResponse> getMyWallet(
            HttpServletRequest request
    ) {
        String userIdStr = (String) request.getAttribute("userId");
        UUID userId = UUID.fromString(userIdStr);

        WalletResponse response = walletService.getMyWallet(userId);

        return  ApiResponse.<WalletResponse>builder()
                .status("success")
                .message("Wallet retrieved successfully")
                .data(response)
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .build();
    }

    @GetMapping("/me/transactions")
    public ApiResponse<PageResponse<WalletTransactionResponse>> getMyWalletTransactions(
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

        Page<WalletTransactionResponse> transactionPage = walletService.getMyTransactions(userId, pageable);

        PageResponse<WalletTransactionResponse> pageResponse =
                PageResponse.<WalletTransactionResponse>builder()
                        .content(transactionPage.getContent())
                        .page(transactionPage.getNumber())
                        .size(transactionPage.getSize())
                        .totalElements(transactionPage.getTotalElements())
                        .totalPages(transactionPage.getTotalPages())
                        .first(transactionPage.isFirst())
                        .last(transactionPage.isLast())
                        .build();

        return ApiResponse.<PageResponse<WalletTransactionResponse>>builder()
                .status("success")
                .message("Wallet transactions retrieved successfully")
                .data(pageResponse)
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .build();
    }

    @GetMapping("/{userId:[0-9a-fA-F\\\\-]{36}}")
    public ApiResponse<AdminWalletResponse> getWalletById(
            HttpServletRequest request,
            @PathVariable UUID userId
    ) {
        String role = (String) request.getAttribute("userRole");

        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("Forbidden");
        }

        AdminWalletResponse response = walletService.getWalletByUserId(userId);

        return  ApiResponse.<AdminWalletResponse>builder()
                .status("success")
                .message("Wallet retrieved successfully")
                .data(response)
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .build();
    }
}
