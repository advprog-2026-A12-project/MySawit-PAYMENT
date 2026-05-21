package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.client.PaymentGatewayClient;
import id.ac.ui.cs.advprog.mysawitpayment.client.XenditProperties;
import id.ac.ui.cs.advprog.mysawitpayment.dto.request.CreateTopUpRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.request.XenditCallbackRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.request.filter.TopUpFilter;
import id.ac.ui.cs.advprog.mysawitpayment.dto.result.CreateInvoiceResult;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.CreateTopUpResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.HistoryTopUpResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.TopUpDetailResponse;
import id.ac.ui.cs.advprog.mysawitpayment.exception.ForbiddenException;
import id.ac.ui.cs.advprog.mysawitpayment.exception.InvalidAmountException;
import id.ac.ui.cs.advprog.mysawitpayment.exception.PaymentTransactionNotFoundException;
import id.ac.ui.cs.advprog.mysawitpayment.model.PaymentTransaction;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.PaymentTransactionStatus;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.UserRole;
import id.ac.ui.cs.advprog.mysawitpayment.repository.PaymentTransactionRepository;
import id.ac.ui.cs.advprog.mysawitpayment.security.AuthenticatedUser;
import id.ac.ui.cs.advprog.mysawitpayment.security.PaymentAuthorizationService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

class TopUpServiceImplTest {

    private PaymentTransactionRepository paymentTransactionRepository;
    private PaymentGatewayClient paymentGatewayClient;
    private XenditProperties xenditProperties;
    private WalletService walletService;
    private PaymentAuthorizationService authorizationService;

    private TopUpServiceImpl service;

    @BeforeEach
    void setUp() {
        paymentTransactionRepository = mock(PaymentTransactionRepository.class);
        paymentGatewayClient = mock(PaymentGatewayClient.class);
        xenditProperties = new XenditProperties();
        walletService = mock(WalletService.class);
        authorizationService = new PaymentAuthorizationService();

        xenditProperties.setWebhookToken("valid-token");

        service = new TopUpServiceImpl(
                paymentTransactionRepository,
                paymentGatewayClient,
                xenditProperties,
                walletService,
                authorizationService
        );
    }

    private AuthenticatedUser adminUser(UUID adminId) {
        return new AuthenticatedUser(adminId, UserRole.ADMIN);
    }

    @Test
    void createTopUpShouldCreateInvoiceAndReturnResponse() {
        UUID adminId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime expiresAt = createdAt.plusHours(1);

        CreateTopUpRequest request = new CreateTopUpRequest();
        setField(request, "amountSawitDollar", new BigDecimal("12.345"));

        PaymentTransaction firstSaved = PaymentTransaction.builder()
                .id(transactionId)
                .adminId(adminId)
                .amountSawitDollar(new BigDecimal("12.35"))
                .amountIdr(new BigDecimal("123500.00"))
                .paymentGateway("XENDIT")
                .status(PaymentTransactionStatus.PENDING)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();

        PaymentTransaction secondSaved = PaymentTransaction.builder()
                .id(transactionId)
                .adminId(adminId)
                .amountSawitDollar(new BigDecimal("12.35"))
                .amountIdr(new BigDecimal("123500.00"))
                .paymentGateway("XENDIT")
                .gatewayReferenceId("inv-123")
                .status(PaymentTransactionStatus.PENDING)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();

        CreateInvoiceResult invoiceResult = CreateInvoiceResult.builder()
                .gatewayReferenceId("inv-123")
                .paymentUrl("https://pay.xendit.co/inv-123")
                .expiresAt(expiresAt)
                .status("PENDING")
                .build();

        when(paymentTransactionRepository.save(any(PaymentTransaction.class)))
                .thenReturn(firstSaved)
                .thenReturn(secondSaved);

        when(paymentGatewayClient.createTopupInvoice(
                eq(transactionId),
                eq(adminId),
                eq(new BigDecimal("123500.00"))
        )).thenReturn(invoiceResult);

        CreateTopUpResponse response = service.createTopUp(request, adminUser(adminId));

        assertThat(response.getId()).isEqualTo(transactionId);
        assertThat(response.getAmountSawitDollar()).isEqualByComparingTo("12.35");
        assertThat(response.getAmountIdr()).isEqualByComparingTo("123500.00");
        assertThat(response.getExchangeRate()).isEqualTo("1 SD = Rp 10,000");
        assertThat(response.getPaymentGateway()).isEqualTo("XENDIT");
        assertThat(response.getStatus()).isEqualTo(PaymentTransactionStatus.PENDING);
        assertThat(response.getPaymentUrl()).isEqualTo("https://pay.xendit.co/inv-123");
        assertThat(response.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);

        verify(paymentTransactionRepository, times(2)).save(any(PaymentTransaction.class));
        verify(paymentGatewayClient).createTopupInvoice(transactionId, adminId, new BigDecimal("123500.00"));
    }

    @Test
    void createTopUpShouldThrowWhenRequestIsNull() {
        UUID adminId = UUID.randomUUID();

        assertThatThrownBy(() -> service.createTopUp(null, adminUser(adminId)))
                .isInstanceOf(InvalidAmountException.class)
                .hasMessage("Amount SawitDollar is required");

        verify(paymentTransactionRepository, never()).save(any());
        verify(paymentGatewayClient, never()).createTopupInvoice(any(), any(), any());
    }

    @Test
    void createTopUpShouldThrowWhenAmountIsNull() {
        UUID adminId = UUID.randomUUID();
        CreateTopUpRequest request = new CreateTopUpRequest();

        assertThatThrownBy(() -> service.createTopUp(request, adminUser(adminId)))
                .isInstanceOf(InvalidAmountException.class)
                .hasMessage("Amount SawitDollar is required");

        verify(paymentTransactionRepository, never()).save(any());
        verify(paymentGatewayClient, never()).createTopupInvoice(any(), any(), any());
    }

    @Test
    void createTopUpShouldThrowWhenAmountIsZero() {
        UUID adminId = UUID.randomUUID();
        CreateTopUpRequest request = new CreateTopUpRequest();
        setField(request, "amountSawitDollar", BigDecimal.ZERO);

        assertThatThrownBy(() -> service.createTopUp(request, adminUser(adminId)))
                .isInstanceOf(InvalidAmountException.class)
                .hasMessage("Amount SawitDollar must be greater than 0");

        verify(paymentTransactionRepository, never()).save(any());
        verify(paymentGatewayClient, never()).createTopupInvoice(any(), any(), any());
    }

    @Test
    void createTopUpShouldThrowWhenAmountIsNegative() {
        UUID adminId = UUID.randomUUID();
        CreateTopUpRequest request = new CreateTopUpRequest();
        setField(request, "amountSawitDollar", new BigDecimal("-1"));

        assertThatThrownBy(() -> service.createTopUp(request, adminUser(adminId)))
                .isInstanceOf(InvalidAmountException.class)
                .hasMessage("Amount SawitDollar must be greater than 0");

        verify(paymentTransactionRepository, never()).save(any());
        verify(paymentGatewayClient, never()).createTopupInvoice(any(), any(), any());
    }

    @Test
    void createTopUpShouldThrowWhenAmountExceedsMaximum() {
        UUID adminId = UUID.randomUUID();
        CreateTopUpRequest request = new CreateTopUpRequest();
        setField(request, "amountSawitDollar", new BigDecimal("100000.01"));

        assertThatThrownBy(() -> service.createTopUp(request, adminUser(adminId)))
                .isInstanceOf(InvalidAmountException.class)
                .hasMessage("Amount SawitDollar must be at most 100000");

        verify(paymentTransactionRepository, never()).save(any());
        verify(paymentGatewayClient, never()).createTopupInvoice(any(), any(), any());
    }

    @Test
    void getMyTopUpsShouldMapPageCorrectly() {
        UUID adminId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        PaymentTransaction tx = PaymentTransaction.builder()
                .id(UUID.randomUUID())
                .adminId(adminId)
                .amountSawitDollar(new BigDecimal("15.00"))
                .amountIdr(new BigDecimal("150000.00"))
                .paymentGateway("XENDIT")
                .status(PaymentTransactionStatus.SUCCESS)
                .createdAt(now.minusHours(1))
                .updatedAt(now)
                .build();

        Page<PaymentTransaction> page = new PageImpl<>(List.of(tx), pageable, 1);
        TopUpFilter filter = new TopUpFilter(PaymentTransactionStatus.SUCCESS);
        when(paymentTransactionRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<HistoryTopUpResponse> result = service.getMyTopUps(adminUser(adminId), filter, pageable);

        assertThat(result.getContent()).hasSize(1);
        HistoryTopUpResponse item = result.getContent().get(0);
        assertThat(item.getId()).isEqualTo(tx.getId());
        assertThat(item.getAmountSawitDollar()).isEqualByComparingTo("15.00");
        assertThat(item.getAmountIdr()).isEqualByComparingTo("150000.00");
        assertThat(item.getPaymentGateway()).isEqualTo("XENDIT");
        assertThat(item.getStatus()).isEqualTo("SUCCESS");
        assertThat(item.getCreatedAt()).isEqualTo(tx.getCreatedAt());
        assertThat(item.getUpdatedAt()).isEqualTo(tx.getUpdatedAt());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void getMyTopUpsShouldBuildSpecificationWithStatusFilter() {
        UUID adminId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        TopUpFilter filter = new TopUpFilter(PaymentTransactionStatus.EXPIRED);

        when(paymentTransactionRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        service.getMyTopUps(adminUser(adminId), filter, pageable);

        ArgumentCaptor<Specification<PaymentTransaction>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(paymentTransactionRepository).findAll(captor.capture(), eq(pageable));

        Root<PaymentTransaction> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get(any(String.class))).thenReturn(path);
        when(cb.equal(any(Expression.class), any(Object.class))).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        Predicate result = captor.getValue().toPredicate(root, query, cb);

        assertThat(result).isNotNull();
        verify(cb, times(2)).equal(any(Expression.class), any(Object.class));
        verify(cb).and(any(Predicate[].class));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void getMyTopUpsShouldBuildSpecificationWithoutStatusFilter() {
        UUID adminId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        when(paymentTransactionRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        service.getMyTopUps(adminUser(adminId), null, pageable);

        ArgumentCaptor<Specification<PaymentTransaction>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(paymentTransactionRepository).findAll(captor.capture(), eq(pageable));

        Root<PaymentTransaction> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get(any(String.class))).thenReturn(path);
        when(cb.equal(any(Expression.class), any(Object.class))).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        Predicate result = captor.getValue().toPredicate(root, query, cb);

        assertThat(result).isNotNull();
        verify(cb).equal(any(Expression.class), eq(adminId));
        verify(cb, times(1)).equal(any(Expression.class), any(Object.class));
        verify(cb).and(any(Predicate[].class));
    }

    @Test
    void handleXenditCallbackShouldThrowWhenTokenIsNull() {
        XenditCallbackRequest request = new XenditCallbackRequest();

        assertThatThrownBy(() -> service.handleXenditCallback(null, request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Invalid Xendit callback token");

        verify(paymentTransactionRepository, never()).findByIdForUpdate(any());
    }

    @Test
    void handleXenditCallbackShouldThrowWhenTokenIsInvalid() {
        XenditCallbackRequest request = new XenditCallbackRequest();

        assertThatThrownBy(() -> service.handleXenditCallback("wrong-token", request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Invalid Xendit callback token");

        verify(paymentTransactionRepository, never()).findByIdForUpdate(any());
    }

    @Test
    void handleXenditCallbackShouldBeTransactional() throws Exception {
        boolean transactional = TopUpServiceImpl.class
                .getMethod("handleXenditCallback", String.class, XenditCallbackRequest.class)
                .isAnnotationPresent(jakarta.transaction.Transactional.class);

        assertThat(transactional).isTrue();
    }

    @Test
    void callbackTransactionLookupShouldUsePessimisticWriteLock() throws Exception {
        org.springframework.data.jpa.repository.Lock lock = PaymentTransactionRepository.class
                .getMethod("findByIdForUpdate", UUID.class)
                .getAnnotation(org.springframework.data.jpa.repository.Lock.class);

        assertThat(lock).isNotNull();
        assertThat(lock.value()).isEqualTo(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
    }

    @Test
    void handleXenditCallbackShouldThrowWhenTransactionNotFound() {
        UUID transactionId = UUID.randomUUID();
        XenditCallbackRequest request = new XenditCallbackRequest();
        request.setId("inv-missing");
        request.setExternalId(transactionId.toString());
        request.setStatus("PAID");
        request.setAmount(new BigDecimal("100000.00"));

        when(paymentTransactionRepository.findByIdForUpdate(transactionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.handleXenditCallback("valid-token", request))
                .isInstanceOf(PaymentTransactionNotFoundException.class)
                .hasMessage("Payment transaction not found");
    }

    @Test
    void handleXenditCallbackShouldThrowWhenExternalIdIsMissing() {
        XenditCallbackRequest request = new XenditCallbackRequest();
        request.setId("inv-1");
        request.setStatus("PAID");
        request.setAmount(new BigDecimal("100000.00"));

        assertThatThrownBy(() -> service.handleXenditCallback("valid-token", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("External id is required");

        verify(paymentTransactionRepository, never()).findByIdForUpdate(any());
    }

    @Test
    void handleXenditCallbackShouldThrowWhenExternalIdIsInvalidUuid() {
        XenditCallbackRequest request = new XenditCallbackRequest();
        request.setId("inv-1");
        request.setExternalId("not-a-uuid");
        request.setStatus("PAID");
        request.setAmount(new BigDecimal("100000.00"));

        assertThatThrownBy(() -> service.handleXenditCallback("valid-token", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("External id must be a valid UUID");

        verify(paymentTransactionRepository, never()).findByIdForUpdate(any());
    }

    @Test
    void handleXenditCallbackShouldThrowWhenStatusIsUnsupported() {
        XenditCallbackRequest request = new XenditCallbackRequest();
        request.setId("inv-unknown");
        request.setExternalId(UUID.randomUUID().toString());
        request.setStatus("WHATEVER");
        request.setAmount(new BigDecimal("100000.00"));

        assertThatThrownBy(() -> service.handleXenditCallback("valid-token", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unsupported Xendit callback status");

        verify(paymentTransactionRepository, never()).findByIdForUpdate(any());
    }

    @Test
    void handleXenditCallbackShouldReturnImmediatelyWhenAlreadySuccess() {
        UUID transactionId = UUID.randomUUID();
        PaymentTransaction transaction = PaymentTransaction.builder()
                .id(transactionId)
                .adminId(UUID.randomUUID())
                .amountSawitDollar(new BigDecimal("10.00"))
                .amountIdr(new BigDecimal("100000.00"))
                .paymentGateway("XENDIT")
                .gatewayReferenceId("inv-1")
                .status(PaymentTransactionStatus.SUCCESS)
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .updatedAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        XenditCallbackRequest request = new XenditCallbackRequest();
        request.setExternalId(transactionId.toString());
        request.setId("inv-1");
        request.setStatus("PAID");
        request.setAmount(new BigDecimal("100000.00"));

        when(paymentTransactionRepository.findByIdForUpdate(transactionId)).thenReturn(Optional.of(transaction));

        service.handleXenditCallback("valid-token", request);

        verify(walletService, never()).creditWallet(any(), any(), any(), any(), any());
        verify(paymentTransactionRepository, never()).save(any());
    }

    @Test
    void handleXenditCallbackShouldMarkSuccessAndCreditWallet() {
        UUID transactionId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        PaymentTransaction transaction = PaymentTransaction.builder()
                .id(transactionId)
                .adminId(adminId)
                .amountSawitDollar(new BigDecimal("10.00"))
                .amountIdr(new BigDecimal("100000.00"))
                .paymentGateway("XENDIT")
                .gatewayReferenceId("inv-paid")
                .status(PaymentTransactionStatus.PENDING)
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .updatedAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        XenditCallbackRequest request = new XenditCallbackRequest();
        request.setId("inv-paid");
        request.setExternalId(transactionId.toString());
        request.setStatus("PAID");
        request.setAmount(new BigDecimal("100000.00"));
        request.setPaidAt(OffsetDateTime.now(ZoneOffset.UTC));

        when(paymentTransactionRepository.findByIdForUpdate(transactionId)).thenReturn(Optional.of(transaction));

        service.handleXenditCallback("valid-token", request);

        assertThat(transaction.getStatus()).isEqualTo(PaymentTransactionStatus.SUCCESS);
        assertThat(transaction.getGatewayReferenceId()).isEqualTo("inv-paid");
        assertThat(transaction.getGatewayCallbackPayload()).isNotNull();
        assertThat(transaction.getGatewayCallbackPayload()).containsEntry("id", "inv-paid");
        assertThat(transaction.getGatewayCallbackPayload()).containsEntry("status", "PAID");

        verify(walletService).creditWallet(
                adminId,
                new BigDecimal("10.00"),
                "TOPUP",
                transactionId,
                "Top-up via Xendit"
        );
        verify(paymentTransactionRepository).save(transaction);
    }

    @Test
    void handleXenditCallbackShouldMarkExpired() {
        UUID transactionId = UUID.randomUUID();

        PaymentTransaction transaction = PaymentTransaction.builder()
                .id(transactionId)
                .adminId(UUID.randomUUID())
                .amountSawitDollar(new BigDecimal("10.00"))
                .amountIdr(new BigDecimal("100000.00"))
                .paymentGateway("XENDIT")
                .gatewayReferenceId("inv-expired")
                .status(PaymentTransactionStatus.PENDING)
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .updatedAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        XenditCallbackRequest request = new XenditCallbackRequest();
        request.setId("inv-expired");
        request.setExternalId(transactionId.toString());
        request.setStatus("EXPIRED");
        request.setAmount(new BigDecimal("100000.00"));

        when(paymentTransactionRepository.findByIdForUpdate(transactionId)).thenReturn(Optional.of(transaction));

        service.handleXenditCallback("valid-token", request);

        assertThat(transaction.getStatus()).isEqualTo(PaymentTransactionStatus.EXPIRED);
        assertThat(transaction.getGatewayReferenceId()).isEqualTo("inv-expired");
        assertThat(transaction.getGatewayCallbackPayload()).isNotNull();

        verify(walletService, never()).creditWallet(any(), any(), any(), any(), any());
        verify(paymentTransactionRepository).save(transaction);
    }

    @Test
    void handleXenditCallbackShouldMarkFailed() {
        UUID transactionId = UUID.randomUUID();

        PaymentTransaction transaction = PaymentTransaction.builder()
                .id(transactionId)
                .adminId(UUID.randomUUID())
                .amountSawitDollar(new BigDecimal("10.00"))
                .amountIdr(new BigDecimal("100000.00"))
                .paymentGateway("XENDIT")
                .gatewayReferenceId("inv-failed")
                .status(PaymentTransactionStatus.PENDING)
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .updatedAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        XenditCallbackRequest request = new XenditCallbackRequest();
        request.setId("inv-failed");
        request.setExternalId(transactionId.toString());
        request.setStatus("FAILED");
        request.setAmount(new BigDecimal("100000.00"));

        when(paymentTransactionRepository.findByIdForUpdate(transactionId)).thenReturn(Optional.of(transaction));

        service.handleXenditCallback("valid-token", request);

        assertThat(transaction.getStatus()).isEqualTo(PaymentTransactionStatus.FAILED);
        assertThat(transaction.getGatewayReferenceId()).isEqualTo("inv-failed");
        assertThat(transaction.getGatewayCallbackPayload()).isNotNull();

        verify(walletService, never()).creditWallet(any(), any(), any(), any(), any());
        verify(paymentTransactionRepository).save(transaction);
    }

    @Test
    void handleXenditCallbackShouldThrowWhenGatewayReferenceIdDoesNotMatch() {
        UUID transactionId = UUID.randomUUID();

        PaymentTransaction transaction = PaymentTransaction.builder()
                .id(transactionId)
                .adminId(UUID.randomUUID())
                .amountSawitDollar(new BigDecimal("10.00"))
                .amountIdr(new BigDecimal("100000.00"))
                .paymentGateway("XENDIT")
                .gatewayReferenceId("existing-ref")
                .status(PaymentTransactionStatus.PENDING)
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .updatedAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        XenditCallbackRequest request = new XenditCallbackRequest();
        request.setId("new-ref");
        request.setExternalId(transactionId.toString());
        request.setStatus("EXPIRED");
        request.setAmount(new BigDecimal("100000.00"));

        when(paymentTransactionRepository.findByIdForUpdate(transactionId)).thenReturn(Optional.of(transaction));

        assertThatThrownBy(() -> service.handleXenditCallback("valid-token", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Xendit callback id does not match transaction gateway reference id");

        assertThat(transaction.getGatewayReferenceId()).isEqualTo("existing-ref");
        assertThat(transaction.getStatus()).isEqualTo(PaymentTransactionStatus.PENDING);
        verify(paymentTransactionRepository, never()).save(transaction);
    }

    @Test
    void handleXenditCallbackShouldThrowWhenAmountDoesNotMatch() {
        UUID transactionId = UUID.randomUUID();

        PaymentTransaction transaction = PaymentTransaction.builder()
                .id(transactionId)
                .adminId(UUID.randomUUID())
                .amountSawitDollar(new BigDecimal("10.00"))
                .amountIdr(new BigDecimal("100000.00"))
                .paymentGateway("XENDIT")
                .gatewayReferenceId("inv-paid")
                .status(PaymentTransactionStatus.PENDING)
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .updatedAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        XenditCallbackRequest request = new XenditCallbackRequest();
        request.setId("inv-paid");
        request.setExternalId(transactionId.toString());
        request.setStatus("PAID");
        request.setAmount(new BigDecimal("99999.00"));

        when(paymentTransactionRepository.findByIdForUpdate(transactionId)).thenReturn(Optional.of(transaction));

        assertThatThrownBy(() -> service.handleXenditCallback("valid-token", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Xendit callback amount does not match transaction amount");

        assertThat(transaction.getStatus()).isEqualTo(PaymentTransactionStatus.PENDING);
        verify(walletService, never()).creditWallet(any(), any(), any(), any(), any());
        verify(paymentTransactionRepository, never()).save(transaction);
    }

    @Test
    void getTopUpDetailShouldReturnMappedResponse() {
        UUID transactionId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.now(ZoneOffset.UTC).minusHours(2);
        OffsetDateTime updatedAt = OffsetDateTime.now(ZoneOffset.UTC);

        PaymentTransaction transaction = PaymentTransaction.builder()
                .id(transactionId)
                .adminId(adminId)
                .amountSawitDollar(new BigDecimal("20.00"))
                .amountIdr(new BigDecimal("200000.00"))
                .paymentGateway("XENDIT")
                .gatewayReferenceId("inv-detail")
                .status(PaymentTransactionStatus.SUCCESS)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        when(paymentTransactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        TopUpDetailResponse response = service.getTopUpDetail(transactionId, adminUser(adminId));

        assertThat(response.getId()).isEqualTo(transactionId);
        assertThat(response.getAdmin()).isNotNull();
        assertThat(response.getAdmin().getId()).isEqualTo(adminId);
        assertThat(response.getAmountSawitDollar()).isEqualByComparingTo("20.00");
        assertThat(response.getAmountIdr()).isEqualByComparingTo("200000.00");
        assertThat(response.getExchangeRate()).isEqualTo("1 SD = Rp 10,000");
        assertThat(response.getPaymentGateway()).isEqualTo("XENDIT");
        assertThat(response.getGatewayReferenceId()).isEqualTo("inv-detail");
        assertThat(response.getStatus()).isEqualTo(PaymentTransactionStatus.SUCCESS);
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
        assertThat(response.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void getTopUpDetailShouldThrowWhenNotFound() {
        UUID transactionId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        when(paymentTransactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getTopUpDetail(transactionId, adminUser(adminId)))
                .isInstanceOf(PaymentTransactionNotFoundException.class)
                .hasMessage("Top-up transaction not found");
    }

    @Test
    void createTopUpShouldThrowForbiddenWhenRequesterIsNotAdmin() {
        CreateTopUpRequest request = new CreateTopUpRequest();
        setField(request, "amountSawitDollar", new BigDecimal("10.00"));

        AuthenticatedUser worker = new AuthenticatedUser(UUID.randomUUID(), UserRole.BURUH);

        assertThatThrownBy(() -> service.createTopUp(request, worker))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Forbidden");

        verify(paymentTransactionRepository, never()).save(any());
        verify(paymentGatewayClient, never()).createTopupInvoice(any(), any(), any());
    }

    @Test
    void getTopUpDetailShouldThrowForbiddenWhenAdminDoesNotOwnTopUp() {
        UUID transactionId = UUID.randomUUID();
        UUID ownerAdminId = UUID.randomUUID();
        UUID otherAdminId = UUID.randomUUID();

        PaymentTransaction transaction = PaymentTransaction.builder()
                .id(transactionId)
                .adminId(ownerAdminId)
                .amountSawitDollar(new BigDecimal("20.00"))
                .amountIdr(new BigDecimal("200000.00"))
                .paymentGateway("XENDIT")
                .status(PaymentTransactionStatus.SUCCESS)
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .updatedAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        when(paymentTransactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        assertThatThrownBy(() -> service.getTopUpDetail(transactionId, adminUser(otherAdminId)))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Forbidden");
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
