package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.dto.response.AdminWalletResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.WalletResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.WalletTransactionResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.result.WalletMutationResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface WalletService {
    AdminWalletResponse getWalletByUserId(UUID userId);

    WalletResponse getMyWallet(UUID userId);

    Page<WalletTransactionResponse> getMyTransactions(UUID userId, Pageable pageable);

    WalletMutationResult creditWallet(UUID userId, BigDecimal amount, String referenceType, UUID referenceId, String description);

    WalletMutationResult debitWallet(UUID userId, BigDecimal amount, String referenceType, UUID referenceId, String description);
}