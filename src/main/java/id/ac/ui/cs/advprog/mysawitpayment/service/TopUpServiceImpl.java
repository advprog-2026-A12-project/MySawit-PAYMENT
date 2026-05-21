package id.ac.ui.cs.advprog.mysawitpayment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.mysawitpayment.client.PaymentGatewayClient;
import id.ac.ui.cs.advprog.mysawitpayment.client.XenditProperties;
import id.ac.ui.cs.advprog.mysawitpayment.dto.request.filter.TopUpFilter;
import id.ac.ui.cs.advprog.mysawitpayment.dto.result.CreateInvoiceResult;
import id.ac.ui.cs.advprog.mysawitpayment.dto.request.XenditCallbackRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.request.CreateTopUpRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.AdminReferenceResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.CreateTopUpResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.HistoryTopUpResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.TopUpDetailResponse;
import id.ac.ui.cs.advprog.mysawitpayment.exception.ForbiddenException;
import id.ac.ui.cs.advprog.mysawitpayment.exception.InvalidAmountException;
import id.ac.ui.cs.advprog.mysawitpayment.exception.PaymentTransactionAlreadyProcessedException;
import id.ac.ui.cs.advprog.mysawitpayment.exception.PaymentTransactionNotFoundException;
import id.ac.ui.cs.advprog.mysawitpayment.model.PaymentTransaction;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.PaymentTransactionStatus;
import id.ac.ui.cs.advprog.mysawitpayment.repository.PaymentTransactionRepository;
import id.ac.ui.cs.advprog.mysawitpayment.security.AuthenticatedUser;
import id.ac.ui.cs.advprog.mysawitpayment.security.PaymentAuthorizationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TopUpServiceImpl implements TopUpService {

    private static final BigDecimal EXCHANGE_RATE = BigDecimal.valueOf(10_000);
    private static final BigDecimal MAX_TOP_UP_AMOUNT = new BigDecimal("100000.00");
    private static final Set<String> SUPPORTED_XENDIT_CALLBACK_STATUSES = Set.of("PAID", "EXPIRED", "FAILED");

    private final PaymentTransactionRepository paymentTransactionRepository;

    private final PaymentGatewayClient paymentGatewayClient;

    private final XenditProperties xenditProperties;

    private final WalletService walletService;

    private final PaymentAuthorizationService authorizationService;

    @Override
    @Transactional
    public CreateTopUpResponse createTopUp(CreateTopUpRequest request, AuthenticatedUser requester) {
        authorizationService.requireAdmin(requester);
        validateCreateTopUpRequest(request);

        BigDecimal amountSawitDollar = request.getAmountSawitDollar().setScale(2, RoundingMode.HALF_UP);
        BigDecimal amountIdr = amountSawitDollar.multiply(EXCHANGE_RATE).setScale(2, RoundingMode.HALF_UP);

        PaymentTransaction paymentTransaction = PaymentTransaction.builder()
                .adminId(requester.id())
                .amountSawitDollar(amountSawitDollar)
                .amountIdr(amountIdr)
                .paymentGateway("XENDIT")
                .status(PaymentTransactionStatus.PENDING)
                .build();

        PaymentTransaction savedTransaction = paymentTransactionRepository.save(paymentTransaction);

        CreateInvoiceResult invoiceResult = paymentGatewayClient.createTopupInvoice(
                savedTransaction.getId(),
                requester.id(),
                amountIdr
        );

        savedTransaction.assignGatewayReferenceId(invoiceResult.getGatewayReferenceId());

        paymentTransactionRepository.save(savedTransaction);

        return CreateTopUpResponse.builder()
                .id(savedTransaction.getId())
                .amountSawitDollar(savedTransaction.getAmountSawitDollar())
                .amountIdr(savedTransaction.getAmountIdr())
                .exchangeRate("1 SD = Rp 10,000")
                .paymentGateway(savedTransaction.getPaymentGateway())
                .status(savedTransaction.getStatus())
                .paymentUrl(invoiceResult.getPaymentUrl())
                .expiresAt(invoiceResult.getExpiresAt())
                .createdAt(savedTransaction.getCreatedAt())
                .build();
    }

    @Override
    public Page<HistoryTopUpResponse> getMyTopUps(
            AuthenticatedUser requester,
            TopUpFilter filter,
            Pageable pageable
    ) {
        authorizationService.requireAdmin(requester);

        return paymentTransactionRepository.findAll(topUpSpec(requester.id(), filter), pageable)
                .map(this::mapToHistoryTopUpResponse);
    }

    @Override
    @Transactional
    public void handleXenditCallback(String callbackToken, XenditCallbackRequest request) {
        validateCallbackToken(callbackToken);

        UUID transactionId = parseCallbackTransactionId(request);
        String callbackStatus = normalizeCallbackStatus(request);

        PaymentTransaction transaction = paymentTransactionRepository.findByIdForUpdate(transactionId)
                .orElseThrow(PaymentTransactionNotFoundException::new);

        validateCallbackMatchesTransaction(transaction, request);

        ObjectMapper mapper = new ObjectMapper();

        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        @SuppressWarnings("unchecked")
        Map<String, Object> payloadMap = mapper.convertValue(request, Map.class);

        PaymentTransactionStatus requestedStatus = mapCallbackStatus(callbackStatus);

        if (transaction.getStatus() != PaymentTransactionStatus.PENDING) {
            if (transaction.getStatus() == requestedStatus) {
                return;
            }
            throw new PaymentTransactionAlreadyProcessedException();
        }

        if (requestedStatus == PaymentTransactionStatus.SUCCESS) {
            transaction.markSuccess(payloadMap);

            walletService.creditWallet(
                    transaction.getAdminId(),
                    transaction.getAmountSawitDollar(),
                    "TOPUP",
                    transaction.getId(),
                    "Top-up via Xendit"
            );
        } else if (requestedStatus == PaymentTransactionStatus.EXPIRED) {
            transaction.markExpired(payloadMap);
        } else if (requestedStatus == PaymentTransactionStatus.FAILED) {
            transaction.markFailed(payloadMap);
        }

        paymentTransactionRepository.save(transaction);
    }

    private PaymentTransactionStatus mapCallbackStatus(String callbackStatus) {
        if ("PAID".equals(callbackStatus)) {
            return PaymentTransactionStatus.SUCCESS;
        }
        if ("EXPIRED".equals(callbackStatus)) {
            return PaymentTransactionStatus.EXPIRED;
        }
        if ("FAILED".equals(callbackStatus)) {
            return PaymentTransactionStatus.FAILED;
        }
        throw new IllegalArgumentException("Unsupported Xendit callback status");
    }

    @Override
    public TopUpDetailResponse getTopUpDetail(UUID id, AuthenticatedUser requester) {
        PaymentTransaction paymentTransaction = paymentTransactionRepository.findById(id)
                .orElseThrow(() -> new PaymentTransactionNotFoundException("Top-up transaction not found"));

        authorizationService.requireTopUpOwner(requester, paymentTransaction.getAdminId());

        return mapToTopUpDetailResponse(paymentTransaction);
    }

    private void validateCreateTopUpRequest(CreateTopUpRequest request) {
        if (request == null || request.getAmountSawitDollar() == null) {
            throw new InvalidAmountException("Amount SawitDollar is required");
        }

        if (request.getAmountSawitDollar().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount SawitDollar must be greater than 0");
        }

        if (request.getAmountSawitDollar().compareTo(MAX_TOP_UP_AMOUNT) > 0) {
            throw new InvalidAmountException("Amount SawitDollar must be at most 100000");
        }
    }

    private void validateCallbackToken(String callbackToken) {
        if (callbackToken == null || !callbackToken.equals(xenditProperties.getWebhookToken())) {
            throw new ForbiddenException("Invalid Xendit callback token");
        }
    }

    private UUID parseCallbackTransactionId(XenditCallbackRequest request) {
        if (request == null || request.getExternalId() == null || request.getExternalId().isBlank()) {
            throw new IllegalArgumentException("External id is required");
        }

        try {
            return UUID.fromString(request.getExternalId());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("External id must be a valid UUID", exception);
        }
    }

    private String normalizeCallbackStatus(XenditCallbackRequest request) {
        if (request == null || request.getStatus() == null || request.getStatus().isBlank()) {
            throw new IllegalArgumentException("Status is required");
        }

        String status = request.getStatus().toUpperCase(Locale.ROOT);
        if (!SUPPORTED_XENDIT_CALLBACK_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Unsupported Xendit callback status");
        }
        return status;
    }

    private void validateCallbackMatchesTransaction(PaymentTransaction transaction, XenditCallbackRequest request) {
        if (request.getId() == null || request.getId().isBlank()) {
            throw new IllegalArgumentException("Xendit callback id is required");
        }

        if (!request.getId().equals(transaction.getGatewayReferenceId())) {
            throw new IllegalArgumentException(
                    "Xendit callback id does not match transaction gateway reference id"
            );
        }

        if (request.getAmount() == null) {
            throw new IllegalArgumentException("Amount is required");
        }

        if (request.getAmount().compareTo(transaction.getAmountIdr()) != 0) {
            throw new IllegalArgumentException("Xendit callback amount does not match transaction amount");
        }
    }

    private Specification<PaymentTransaction> topUpSpec(UUID adminId, TopUpFilter filter) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("adminId"), adminId));

            if (filter != null && filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }

            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }

    private HistoryTopUpResponse mapToHistoryTopUpResponse(PaymentTransaction paymentTransaction) {
        return HistoryTopUpResponse.builder()
                .id(paymentTransaction.getId())
                .amountSawitDollar(paymentTransaction.getAmountSawitDollar())
                .amountIdr(paymentTransaction.getAmountIdr())
                .paymentGateway(paymentTransaction.getPaymentGateway())
                .status(paymentTransaction.getStatus().name())
                .createdAt(paymentTransaction.getCreatedAt())
                .updatedAt(paymentTransaction.getUpdatedAt())
                .build();
    }

    private TopUpDetailResponse mapToTopUpDetailResponse(PaymentTransaction paymentTransaction) {
        return TopUpDetailResponse.builder()
                .id(paymentTransaction.getId())
                .admin(AdminReferenceResponse.builder()
                        .id(paymentTransaction.getAdminId())
                        .build())
                .amountSawitDollar(paymentTransaction.getAmountSawitDollar())
                .amountIdr(paymentTransaction.getAmountIdr())
                .exchangeRate("1 SD = Rp 10,000")
                .paymentGateway(paymentTransaction.getPaymentGateway())
                .gatewayReferenceId(paymentTransaction.getGatewayReferenceId())
                .status(paymentTransaction.getStatus())
                .createdAt(paymentTransaction.getCreatedAt())
                .updatedAt(paymentTransaction.getUpdatedAt())
                .build();
    }
}
