package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.model.Wallet;

import java.math.BigDecimal;
import java.util.UUID;

public interface WalletService {

    Wallet createWallet(UUID userId);

    Wallet getWalletByUserId(UUID userId);

    void credit(UUID userId, BigDecimal amount, String referenceType, UUID referenceId);

    void debit(UUID userId, BigDecimal amount, String referenceType, UUID referenceId);
}