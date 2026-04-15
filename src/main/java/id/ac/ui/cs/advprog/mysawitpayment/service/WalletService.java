package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.dto.response.AdminWalletResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.WalletResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.WalletTransactionResponse;
import id.ac.ui.cs.advprog.mysawitpayment.model.Wallet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface WalletService {

    Wallet createWallet(UUID userId);

    AdminWalletResponse getWalletByUserId(UUID userId);

    WalletResponse getMyWallet(UUID userId);

    Page<WalletTransactionResponse> getMyTransactions(UUID userId, Pageable pageable);

    void credit(UUID userId, BigDecimal amount, String referenceType, UUID referenceId);

    void debit(UUID userId, BigDecimal amount, String referenceType, UUID referenceId);
}