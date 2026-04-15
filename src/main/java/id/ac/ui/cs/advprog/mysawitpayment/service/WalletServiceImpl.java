package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.dto.response.AdminWalletResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.WalletResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.WalletTransactionResponse;
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
    @Transactional
    public Wallet createWallet(UUID userId) {
        if (walletRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException("Wallet already exists for user");
        }

        Wallet wallet = Wallet.builder()
                .userId(userId)
                .build();

        return walletRepository.save(wallet);
    }

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
        Page<WalletTransaction> walletPage = walletTransactionRepository.findByWalletId(walletId, pageable);
        return walletPage.map(this::mapToWalletTransactionResponse);
    }

    @Override
    @Transactional
    public void credit(UUID userId, BigDecimal amount, String referenceType, UUID referenceId) {

        Wallet wallet = findWalletOrThrow(userId);

        BigDecimal before = wallet.getBalance();

        wallet.credit(amount);

        BigDecimal after = wallet.getBalance();

        walletRepository.save(wallet);

        WalletTransaction transaction = WalletTransaction.builder()
                .walletId(wallet.getId())
                .transactionType(TransactionType.CREDIT)
                .amount(amount)
                .balanceBefore(before)
                .balanceAfter(after)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .description("Credit transaction")
                .build();

        walletTransactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void debit(UUID userId, BigDecimal amount, String referenceType, UUID referenceId) {

        Wallet wallet = findWalletOrThrow(userId);

        BigDecimal before = wallet.getBalance();

        wallet.debit(amount);

        BigDecimal after = wallet.getBalance();

        walletRepository.save(wallet);

        WalletTransaction transaction = WalletTransaction.builder()
                .walletId(wallet.getId())
                .transactionType(TransactionType.DEBIT)
                .amount(amount)
                .balanceBefore(before)
                .balanceAfter(after)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .description("Debit transaction")
                .build();

        walletTransactionRepository.save(transaction);
    }

    private Wallet findWalletOrThrow(UUID userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
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