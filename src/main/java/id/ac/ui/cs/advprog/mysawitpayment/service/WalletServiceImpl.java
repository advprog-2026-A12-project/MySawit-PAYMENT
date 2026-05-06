package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.dto.response.AdminWalletResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.WalletResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.WalletTransactionResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.result.WalletMutationResult;
import id.ac.ui.cs.advprog.mysawitpayment.exception.WalletNotFoundException;
import id.ac.ui.cs.advprog.mysawitpayment.model.Wallet;
import id.ac.ui.cs.advprog.mysawitpayment.model.WalletTransaction;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.TransactionType;
import id.ac.ui.cs.advprog.mysawitpayment.repository.WalletRepository;
import id.ac.ui.cs.advprog.mysawitpayment.repository.WalletTransactionRepository;

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

    @Override
    public WalletResponse getMyWallet(UUID userId) {
        return mapToMyWalletResponse(findWalletOrThrow(userId));
    }

    @Override
    public AdminWalletResponse getWalletByUserId(UUID userId) {
        return mapToAdminWalletResponse(findWalletOrThrow(userId));
    }

    @Override
    public Page<WalletTransactionResponse> getMyTransactions(UUID userId, Pageable pageable) {
        UUID walletId = findWalletOrThrow(userId).getId();
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