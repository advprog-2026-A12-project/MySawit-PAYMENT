package id.ac.ui.cs.advprog.mysawitpayment.controller;

import id.ac.ui.cs.advprog.mysawitpayment.dto.request.internal.PayrollCreationRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.request.internal.WalletCreationRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.ApiResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.internal.PayrollCreationResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.internal.WalletCreationResponse;
import id.ac.ui.cs.advprog.mysawitpayment.service.InternalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;


@RestController
@RequestMapping("/api/v1/internal")
@RequiredArgsConstructor
public class InternalController {

    private final InternalService internalService;

    @PostMapping("/payrolls")
    public ApiResponse<PayrollCreationResponse> createPayroll(
            @Valid @RequestBody PayrollCreationRequest request
    ) {
        PayrollCreationResponse response = internalService.createPayroll(request);

        String message = response.isAlreadyProcessed()
                ? "Payroll already exists"
                : "Payroll created successfully";

        return ApiResponse.<PayrollCreationResponse>builder()
                .status("success")
                .message(message)
                .data(response)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @PostMapping("/wallets")
    public ApiResponse<WalletCreationResponse> createWallet(
            @Valid @RequestBody WalletCreationRequest request
    ) {
        WalletCreationResponse response = internalService.createWallet(request);

        String message = response.isAlreadyProcessed()
                ? "Wallet already exists"
                : "Wallet created successfully";

        return ApiResponse.<WalletCreationResponse>builder()
                .status("success")
                .message(message)
                .data(response)
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
