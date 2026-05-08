package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.dto.request.internal.WalletCreationRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.AdminWalletResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.WalletResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.WalletTransactionResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.internal.WalletCreationResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.result.WalletMutationResult;
import id.ac.ui.cs.advprog.mysawitpayment.exception.WalletNotFoundException;
import id.ac.ui.cs.advprog.mysawitpayment.model.Wallet;
import id.ac.ui.cs.advprog.mysawitpayment.model.WalletTransaction;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.TransactionType;
import id.ac.ui.cs.advprog.mysawitpayment.repository.WalletRepository;
import id.ac.ui.cs.advprog.mysawitpayment.repository.WalletTransactionRepository;
import id.ac.ui.cs.advprog.mysawitpayment.security.AuthenticatedUser;
import id.ac.ui.cs.advprog.mysawitpayment.security.PaymentAuthorizationService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final PaymentAuthorizationService authorizationService;

    @Override
    public WalletResponse getMyWallet(AuthenticatedUser requester) {
        authorizationService.requireOwnWalletAccess(requester);
        return mapToMyWalletResponse(findWalletOrThrow(requester.id()));
    }

    @Override
    public AdminWalletResponse getWalletByUserId(AuthenticatedUser requester, UUID userId) {
        authorizationService.requireAdminWalletViewer(requester);
        return mapToAdminWalletResponse(findWalletOrThrow(userId));
    }

    @Override
    public Page<WalletTransactionResponse> getMyTransactions(AuthenticatedUser requester, Pageable pageable) {
        authorizationService.requireOwnWalletAccess(requester);
        UUID walletId = findWalletOrThrow(requester.id()).getId();
        Page<WalletTransaction> transactionPage = walletTransactionRepository.findByWalletId(walletId, pageable);
        return transactionPage.map(this::mapToWalletTransactionResponse);
    }

    @Override
    @Transactional
    public WalletMutationResult creditWallet(
            UUID userId,
            BigDecimal amount,
            String referenceType,
            UUID referenceId,
            String description
    ) {

        Wallet wallet = findWalletOrThrow(userId);

        BigDecimal balanceBefore = wallet.getBalance();
        wallet.credit(amount);
        walletRepository.save(wallet);
        BigDecimal balanceAfter = wallet.getBalance();

        WalletTransaction walletTransaction = WalletTransaction.builder()
                .walletId(wallet.getId())
                .transactionType(TransactionType.CREDIT)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .description(description)
                .build();

        walletTransactionRepository.save(walletTransaction);

        return WalletMutationResult.builder()
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .build();
    }

    @Override
    @Transactional
    public WalletMutationResult debitWallet(
            UUID userId,
            BigDecimal amount,
            String referenceType,
            UUID referenceId,
            String description
    ) {
        Wallet wallet = findWalletOrThrow(userId);

        BigDecimal balanceBefore = wallet.getBalance();
        wallet.debit(amount);
        walletRepository.save(wallet);
        BigDecimal balanceAfter = wallet.getBalance();

        WalletTransaction walletTransaction = WalletTransaction.builder()
                .walletId(wallet.getId())
                .transactionType(TransactionType.DEBIT)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .description(description)
                .build();

        walletTransactionRepository.save(walletTransaction);

        return WalletMutationResult.builder()
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .build();
    }

    @Override
    @Transactional
    public WalletCreationResponse createWallet(WalletCreationRequest request) {
        UUID userId = request.getUserId();

        return walletRepository.findByUserId(userId)
                .map(wallet -> WalletCreationResponse.builder()
                        .walletId(wallet.getId())
                        .alreadyProcessed(true)
                        .build())
                .orElseGet(() -> {
                    Wallet wallet = Wallet.builder()
                            .userId(userId)
                            .build();

                    Wallet savedWallet = walletRepository.save(wallet);

                    return WalletCreationResponse.builder()
                            .walletId(savedWallet.getId())
                            .alreadyProcessed(false)
                            .build();
                });
    }

    private Wallet findWalletOrThrow(UUID userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(WalletNotFoundException::new);
    }

    private WalletResponse mapToMyWalletResponse(Wallet wallet) {
        WalletResponse response = new WalletResponse();
        response.setId(wallet.getId());
        response.setUserId(wallet.getUserId());
        response.setBalance(wallet.getBalance());
        response.setCreatedAt(wallet.getCreatedAt());
        response.setCurrency("SawitDollar");
        response.setUpdatedAt(wallet.getUpdatedAt());
        return response;
    }

    private AdminWalletResponse mapToAdminWalletResponse(Wallet wallet) {
        AdminWalletResponse response = new AdminWalletResponse();
        response.setId(wallet.getId());
        response.setUserId(wallet.getUserId());
        response.setBalance(wallet.getBalance());
        response.setCreatedAt(wallet.getCreatedAt());
        response.setCurrency("SawitDollar");
        response.setUpdatedAt(wallet.getUpdatedAt());
        // TODO: ambil field null ini dari auth
        response.setUserName(null);
        response.setUserRole(null);
        return response;
    }

    private WalletTransactionResponse mapToWalletTransactionResponse(WalletTransaction walletTransaction) {
        WalletTransactionResponse response = new WalletTransactionResponse();
        response.setId(walletTransaction.getId());
        response.setTransactionType(walletTransaction.getTransactionType().name());
        response.setAmount(walletTransaction.getAmount());
        response.setBalanceBefore(walletTransaction.getBalanceBefore());
        response.setBalanceAfter(walletTransaction.getBalanceAfter());
        response.setReferenceType(walletTransaction.getReferenceType());
        response.setReferenceId(walletTransaction.getReferenceId());
        response.setDescription(walletTransaction.getDescription());
        response.setCreatedAt(walletTransaction.getCreatedAt());
        return response;
    }

}
