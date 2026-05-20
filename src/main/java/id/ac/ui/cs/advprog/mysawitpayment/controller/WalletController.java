package id.ac.ui.cs.advprog.mysawitpayment.controller;


import id.ac.ui.cs.advprog.mysawitpayment.dto.request.filter.WalletTransactionFilter;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.ApiResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.AdminWalletResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PageResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.WalletResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.WalletTransactionResponse;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.TransactionType;
import id.ac.ui.cs.advprog.mysawitpayment.security.AuthenticatedUser;
import id.ac.ui.cs.advprog.mysawitpayment.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private static final Map<String, String> TRANSACTION_SORT_FIELDS = Map.of(
            "createdAt", "createdAt",
            "amount", "amount"
    );

    private final WalletService walletService;

    @GetMapping("/me")
    public ApiResponse<WalletResponse> getMyWallet(
            HttpServletRequest request
    ) {
        AuthenticatedUser requester = AuthenticatedUser.from(request);

        WalletResponse response = walletService.getMyWallet(requester);

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
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) TransactionType transactionType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        AuthenticatedUser requester = AuthenticatedUser.from(request);

        Pageable pageable = PageableRequest.of(
                page,
                size,
                sort,
                TRANSACTION_SORT_FIELDS,
                "createdAt,desc"
        );

        WalletTransactionFilter filter = new WalletTransactionFilter(
                transactionType,
                PageableRequest.startOfDay(dateFrom),
                PageableRequest.startOfNextDay(dateTo)
        );

        Page<WalletTransactionResponse> transactionPage =
                walletService.getMyTransactions(requester, filter, pageable);

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
    public ApiResponse<AdminWalletResponse> getWalletByUserId(
            HttpServletRequest request,
            @PathVariable UUID userId
    ) {
        AuthenticatedUser requester = AuthenticatedUser.from(request);

        AdminWalletResponse response = walletService.getWalletByUserId(requester, userId);

        return  ApiResponse.<AdminWalletResponse>builder()
                .status("success")
                .message("Wallet retrieved successfully")
                .data(response)
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .build();
    }
}
