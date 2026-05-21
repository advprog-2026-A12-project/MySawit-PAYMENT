package id.ac.ui.cs.advprog.mysawitpayment.model;

import id.ac.ui.cs.advprog.mysawitpayment.exception.GatewayReferenceAlreadyAssignedException;
import id.ac.ui.cs.advprog.mysawitpayment.exception.PaymentTransactionAlreadyProcessedException;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.PaymentTransactionStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentTransactionTest {

    private PaymentTransaction createPendingTransaction() {
        PaymentTransaction tx = PaymentTransaction.builder()
                .adminId(UUID.randomUUID())
                .amountSawitDollar(new BigDecimal("10"))
                .amountIdr(new BigDecimal("100000"))
                .paymentGateway("XENDIT")
                .build();

        tx.onCreate();
        return tx;
    }

    @Test
    void shouldDefaultStatusToPendingOnCreate() {
        PaymentTransaction tx = createPendingTransaction();

        assertThat(tx.getStatus()).isEqualTo(PaymentTransactionStatus.PENDING);
        assertThat(tx.getCreatedAt()).isNotNull();
        assertThat(tx.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldMarkSuccessWhenPending() {
        PaymentTransaction tx = createPendingTransaction();

        Map<String, Object> payload = Map.of("status", "PAID");

        tx.markSuccess(payload);

        assertThat(tx.getStatus()).isEqualTo(PaymentTransactionStatus.SUCCESS);
        assertThat(tx.getGatewayCallbackPayload()).isEqualTo(payload);
    }

    @Test
    void shouldMarkExpiredWhenPending() {
        PaymentTransaction tx = createPendingTransaction();

        Map<String, Object> payload = Map.of("status", "EXPIRED");

        tx.markExpired(payload);

        assertThat(tx.getStatus()).isEqualTo(PaymentTransactionStatus.EXPIRED);
        assertThat(tx.getGatewayCallbackPayload()).isEqualTo(payload);
    }

    @Test
    void shouldMarkFailedWhenPending() {
        PaymentTransaction tx = createPendingTransaction();

        Map<String, Object> payload = Map.of("status", "FAILED");

        tx.markFailed(payload);

        assertThat(tx.getStatus()).isEqualTo(PaymentTransactionStatus.FAILED);
        assertThat(tx.getGatewayCallbackPayload()).isEqualTo(payload);
    }

    @Test
    void shouldThrowExceptionWhenMarkSuccessNotPending() {
        PaymentTransaction tx = createPendingTransaction();
        tx.markSuccess(Map.of());

        assertThatThrownBy(() -> tx.markFailed(Map.of()))
                .isInstanceOf(PaymentTransactionAlreadyProcessedException.class);
    }

    @Test
    void shouldAssignGatewayReferenceIdOnce() {
        PaymentTransaction tx = createPendingTransaction();

        tx.assignGatewayReferenceId("ref-123");

        assertThat(tx.getGatewayReferenceId()).isEqualTo("ref-123");
    }

    @Test
    void shouldAssignGatewayInvoiceMetadata() {
        PaymentTransaction tx = createPendingTransaction();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusHours(1);

        tx.assignGatewayInvoice("ref-123", "https://pay.xendit.co/ref-123", expiresAt);

        assertThat(tx.getGatewayReferenceId()).isEqualTo("ref-123");
        assertThat(tx.getPaymentUrl()).isEqualTo("https://pay.xendit.co/ref-123");
        assertThat(tx.getExpiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void shouldThrowWhenAssignGatewayReferenceIdTwice() {
        PaymentTransaction tx = createPendingTransaction();

        tx.assignGatewayReferenceId("ref-123");

        assertThatThrownBy(() -> tx.assignGatewayReferenceId("ref-456"))
                .isInstanceOf(GatewayReferenceAlreadyAssignedException.class);
    }

    @Test
    void shouldUpdateUpdatedAtOnUpdate() throws Exception {
        PaymentTransaction tx = createPendingTransaction();

        OffsetDateTime beforeUpdate = tx.getUpdatedAt();

        Thread.sleep(1);

        tx.onUpdate();

        OffsetDateTime afterUpdate = tx.getUpdatedAt();

        assertThat(afterUpdate).isNotEqualTo(beforeUpdate);
    }

    @Test
    void shouldNotOverrideStatusIfAlreadySetBeforePersist() {
        PaymentTransaction tx = PaymentTransaction.builder()
                .adminId(UUID.randomUUID())
                .amountSawitDollar(new BigDecimal("10"))
                .amountIdr(new BigDecimal("100000"))
                .paymentGateway("XENDIT")
                .status(PaymentTransactionStatus.FAILED)
                .build();

        tx.onCreate();

        assertThat(tx.getStatus()).isEqualTo(PaymentTransactionStatus.FAILED);
    }
}
