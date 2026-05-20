package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.dto.request.internal.WalletCreationRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.AdminWalletResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.WalletResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.WalletTransactionResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.internal.WalletCreationResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.result.WalletMutationResult;
import id.ac.ui.cs.advprog.mysawitpayment.dto.request.filter.WalletTransactionFilter;
import id.ac.ui.cs.advprog.mysawitpayment.security.AuthenticatedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface WalletService {
    AdminWalletResponse getWalletByUserId(AuthenticatedUser requester, UUID userId);

    WalletResponse getMyWallet(AuthenticatedUser requester);

    Page<WalletTransactionResponse> getMyTransactions(
            AuthenticatedUser requester,
            WalletTransactionFilter filter,
            Pageable pageable
    );

    WalletMutationResult creditWallet(
            UUID userId,
            BigDecimal amount,
            String referenceType,
            UUID referenceId,
            String description
    );

    WalletMutationResult debitWallet(
            UUID userId,
            BigDecimal amount,
            String referenceType,
            UUID referenceId,
            String description
    );

    WalletCreationResponse createWallet(WalletCreationRequest request);
}
