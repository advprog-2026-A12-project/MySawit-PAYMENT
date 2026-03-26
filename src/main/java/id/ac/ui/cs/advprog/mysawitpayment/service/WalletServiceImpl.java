package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.model.Wallet;
import id.ac.ui.cs.advprog.mysawitpayment.model.WalletTransaction;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.TransactionType;
import id.ac.ui.cs.advprog.mysawitpayment.repository.WalletRepository;
import id.ac.ui.cs.advprog.mysawitpayment.repository.WalletTransactionRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
    public Wallet getWalletByUserId(UUID userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
    }

    @Override
    @Transactional
    public void credit(UUID userId, BigDecimal amount, String referenceType, UUID referenceId) {

        Wallet wallet = getWalletByUserId(userId);

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

        Wallet wallet = getWalletByUserId(userId);

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
}