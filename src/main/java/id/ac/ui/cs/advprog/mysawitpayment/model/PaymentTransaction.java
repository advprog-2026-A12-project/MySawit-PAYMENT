package id.ac.ui.cs.advprog.mysawitpayment.model;

import id.ac.ui.cs.advprog.mysawitpayment.exception.GatewayReferenceAlreadyAssignedException;
import id.ac.ui.cs.advprog.mysawitpayment.exception.PaymentTransactionAlreadyProcessedException;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.PaymentTransactionStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "payment_transactions")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "admin_id", nullable = false)
    private UUID adminId;

    @Column(name = "amount_sawit_dollar", nullable = false, precision = 15, scale = 2)
    private BigDecimal amountSawitDollar;

    @Column(name = "amount_idr", nullable = false, precision = 15, scale = 2)
    private BigDecimal amountIdr;

    @Column(name = "payment_gateway", nullable = false, length = 30)
    private String paymentGateway;

    @Column(name = "gateway_reference_id", length = 255)
    private String gatewayReferenceId;

    @Column(name = "payment_url", length = 2048)
    private String paymentUrl;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "gateway_callback_payload")
    private Map<String, Object> gatewayCallbackPayload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentTransactionStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        this.createdAt = now;
        this.updatedAt = now;

        if (this.status == null) {
            this.status = PaymentTransactionStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public void markSuccess(Map<String, Object> callbackPayload) {
        ensurePending();
        this.status = PaymentTransactionStatus.SUCCESS;
        this.gatewayCallbackPayload = callbackPayload;
    }

    public void markExpired(Map<String, Object> callbackPayload) {
        ensurePending();
        this.status = PaymentTransactionStatus.EXPIRED;
        this.gatewayCallbackPayload = callbackPayload;
    }

    public void markFailed(Map<String, Object> callbackPayload) {
        ensurePending();
        this.status = PaymentTransactionStatus.FAILED;
        this.gatewayCallbackPayload = callbackPayload;
    }

    public void assignGatewayReferenceId(String gatewayReferenceId) {
        if (this.gatewayReferenceId != null) {
            throw new GatewayReferenceAlreadyAssignedException();
        }
        this.gatewayReferenceId = gatewayReferenceId;
    }

    public void assignGatewayInvoice(String gatewayReferenceId, String paymentUrl, OffsetDateTime expiresAt) {
        assignGatewayReferenceId(gatewayReferenceId);
        this.paymentUrl = paymentUrl;
        this.expiresAt = expiresAt;
    }

    private void ensurePending() {
        if (this.status != PaymentTransactionStatus.PENDING) {
            throw new PaymentTransactionAlreadyProcessedException();
        }
    }
}
