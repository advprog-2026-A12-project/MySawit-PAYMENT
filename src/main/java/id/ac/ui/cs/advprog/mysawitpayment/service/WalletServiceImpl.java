package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.dto.request.internal.WalletCreationRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.request.filter.WalletTransactionFilter;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
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
    public Page<WalletTransactionResponse> getMyTransactions(
            AuthenticatedUser requester,
            WalletTransactionFilter filter,
            Pageable pageable
    ) {
        authorizationService.requireOwnWalletAccess(requester);
        UUID walletId = findWalletOrThrow(requester.id()).getId();
        Page<WalletTransaction> transactionPage =
                walletTransactionRepository.findAll(walletTransactionSpec(walletId, filter), pageable);
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

        Wallet wallet = findWalletForUpdateOrThrow(userId);

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
        Wallet wallet = findWalletForUpdateOrThrow(userId);

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
                    UUID walletId = UUID.randomUUID();
                    int insertedRows = walletRepository.insertIfAbsent(walletId, userId);

                    if (insertedRows == 0) {
                        Wallet existingWallet = findWalletOrThrow(userId);
                        return WalletCreationResponse.builder()
                                .walletId(existingWallet.getId())
                                .alreadyProcessed(true)
                                .build();
                    }

                    return WalletCreationResponse.builder()
                            .walletId(walletId)
                            .alreadyProcessed(false)
                            .build();
                });
    }

    private Wallet findWalletOrThrow(UUID userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(WalletNotFoundException::new);
    }

    private Wallet findWalletForUpdateOrThrow(UUID userId) {
        return walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(WalletNotFoundException::new);
    }

    private Specification<WalletTransaction> walletTransactionSpec(
            UUID walletId,
            WalletTransactionFilter filter
    ) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("walletId"), walletId));

            if (filter != null && filter.transactionType() != null) {
                predicates.add(cb.equal(root.get("transactionType"), filter.transactionType()));
            }

            addDateRangePredicates(predicates, cb, root.get("createdAt"), filter);

            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }

    private void addDateRangePredicates(
            List<jakarta.persistence.criteria.Predicate> predicates,
            jakarta.persistence.criteria.CriteriaBuilder cb,
            jakarta.persistence.criteria.Path<OffsetDateTime> path,
            WalletTransactionFilter filter
    ) {
        if (filter == null) {
            return;
        }

        if (filter.dateFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(path, filter.dateFrom()));
        }

        if (filter.dateTo() != null) {
            predicates.add(cb.lessThan(path, filter.dateTo()));
        }
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
