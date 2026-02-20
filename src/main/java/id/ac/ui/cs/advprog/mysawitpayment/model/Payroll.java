package id.ac.ui.cs.advprog.mysawitpayment.model;

import id.ac.ui.cs.advprog.mysawitpayment.model.enums.PayrollStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Column;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "payrolls",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"referenceId", "referenceType"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Double kilogram;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PayrollStatus status;

    @Column(nullable = false)
    private String referenceId;

    @Column(nullable = false)
    private String referenceType;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}