package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.dto.request.internal.WalletCreationRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.request.filter.WalletTransactionFilter;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.AdminWalletResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.WalletResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.WalletTransactionResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.internal.WalletCreationResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.result.WalletMutationResult;
import id.ac.ui.cs.advprog.mysawitpayment.exception.WalletNotFoundException;
import id.ac.ui.cs.advprog.mysawitpayment.mapper.WalletResponseMapper;
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
    private final WalletResponseMapper walletResponseMapper;

    @Override
    public WalletResponse getMyWallet(AuthenticatedUser requester) {
        authorizationService.requireOwnWalletAccess(requester);
        return walletResponseMapper.toWalletResponse(findWalletOrThrow(requester.id()));
    }

    @Override
    public AdminWalletResponse getWalletByUserId(AuthenticatedUser requester, UUID userId) {
        authorizationService.requireAdminWalletViewer(requester);
        return walletResponseMapper.toAdminWalletResponse(findWalletOrThrow(userId));
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
        return transactionPage.map(walletResponseMapper::toWalletTransactionResponse);
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
        return mutateWallet(userId, amount, referenceType, referenceId, description, TransactionType.CREDIT);
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
        return mutateWallet(userId, amount, referenceType, referenceId, description, TransactionType.DEBIT);
    }

    private WalletMutationResult mutateWallet(
            UUID userId,
            BigDecimal amount,
            String referenceType,
            UUID referenceId,
            String description,
            TransactionType transactionType
    ) {
        Wallet wallet = findWalletForUpdateOrThrow(userId);
        var existingTransaction = walletTransactionRepository.findByReferenceTypeAndReferenceIdAndTransactionType(
                referenceType,
                referenceId,
                transactionType
        );
        if (existingTransaction.isPresent()) {
            return mapToWalletMutationResult(existingTransaction.get());
        }

        BigDecimal balanceBefore = wallet.getBalance();
        applyWalletMutation(wallet, amount, transactionType);
        walletRepository.save(wallet);
        BigDecimal balanceAfter = wallet.getBalance();

        WalletTransaction walletTransaction = WalletTransaction.builder()
                .walletId(wallet.getId())
                .transactionType(transactionType)
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

    private void applyWalletMutation(Wallet wallet, BigDecimal amount, TransactionType transactionType) {
        if (TransactionType.CREDIT.equals(transactionType)) {
            wallet.credit(amount);
            return;
        }

        wallet.debit(amount);
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

    private WalletMutationResult mapToWalletMutationResult(WalletTransaction walletTransaction) {
        return WalletMutationResult.builder()
                .balanceBefore(walletTransaction.getBalanceBefore())
                .balanceAfter(walletTransaction.getBalanceAfter())
                .build();
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

}
