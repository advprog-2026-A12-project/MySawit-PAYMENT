package id.ac.ui.cs.advprog.mysawitpayment.model;

import id.ac.ui.cs.advprog.mysawitpayment.model.enums.PayrollStatus;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.ReferenceType;
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
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "payrolls")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payroll {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String userRole;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal kilogram;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal ratePerKg;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal multiplier;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayrollStatus status = PayrollStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReferenceType referenceType;

    @Column(nullable = false)
    private UUID referenceId;

    private UUID approvedBy;

    private OffsetDateTime approvedAt;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}