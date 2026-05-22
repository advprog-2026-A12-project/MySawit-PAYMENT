package id.ac.ui.cs.advprog.mysawitpayment.mapper;

import id.ac.ui.cs.advprog.mysawitpayment.dto.response.AdminWalletResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.WalletResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.WalletTransactionResponse;
import id.ac.ui.cs.advprog.mysawitpayment.model.Wallet;
import id.ac.ui.cs.advprog.mysawitpayment.model.WalletTransaction;
import org.springframework.stereotype.Component;

@Component
public class WalletResponseMapper {

    private static final String CURRENCY_SAWIT_DOLLAR = "SawitDollar";

    public WalletResponse toWalletResponse(Wallet wallet) {
        WalletResponse response = new WalletResponse();
        response.setId(wallet.getId());
        response.setUserId(wallet.getUserId());
        response.setBalance(wallet.getBalance());
        response.setCreatedAt(wallet.getCreatedAt());
        response.setCurrency(CURRENCY_SAWIT_DOLLAR);
        response.setUpdatedAt(wallet.getUpdatedAt());
        return response;
    }

    public AdminWalletResponse toAdminWalletResponse(Wallet wallet) {
        AdminWalletResponse response = new AdminWalletResponse();
        response.setId(wallet.getId());
        response.setUserId(wallet.getUserId());
        response.setBalance(wallet.getBalance());
        response.setCreatedAt(wallet.getCreatedAt());
        response.setCurrency(CURRENCY_SAWIT_DOLLAR);
        response.setUpdatedAt(wallet.getUpdatedAt());
        return response;
    }

    public WalletTransactionResponse toWalletTransactionResponse(WalletTransaction walletTransaction) {
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
