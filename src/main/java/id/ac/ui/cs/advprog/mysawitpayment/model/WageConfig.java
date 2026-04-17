package id.ac.ui.cs.advprog.mysawitpayment.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Column;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "wage_configs")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WageConfig {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "upah_buruh_per_kg", nullable = false, precision = 12, scale = 2)
    @DecimalMin(value = "0.01", message = "Upah buruh per kg must be greater than 0")
    private BigDecimal upahBuruhPerKg;

    @Column(name = "upah_supir_per_kg", nullable = false, precision = 12, scale = 2)
    @DecimalMin(value = "0.01", message = "Upah supir per kg must be greater than 0")
    private BigDecimal upahSupirPerKg;

    @Column(name = "upah_mandor_per_kg", nullable = false, precision = 12, scale = 2)
    @DecimalMin(value = "0.01", message = "Upah mandor per kg must be greater than 0")
    private BigDecimal upahMandorPerKg;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "updated_by", nullable = false)
    private UUID updatedBy;

    @Column(name = "effective_from", nullable = false)
    private OffsetDateTime effectiveFrom;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        if (this.effectiveFrom == null) {
            this.effectiveFrom = now;
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
    }

    public void deactivate() {
        this.isActive = false;
    }

    public boolean isCurrentlyActive() {
        return Boolean.TRUE.equals(this.isActive);
    }
}