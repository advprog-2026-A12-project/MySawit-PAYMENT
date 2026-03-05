package id.ac.ui.cs.advprog.mysawitpayment.repository;

import id.ac.ui.cs.advprog.mysawitpayment.model.Payroll;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.PayrollStatus;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.ReferenceType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface PayrollRepository extends JpaRepository<Payroll, UUID> {

    Page<Payroll> findByUserId(UUID userId, Pageable pageable);

    Page<Payroll> findByUserIdAndStatus(
            UUID userId,
            PayrollStatus status,
            Pageable pageable
    );

    Page<Payroll> findByUserIdAndCreatedAtBetween(
            UUID userId,
            OffsetDateTime start,
            OffsetDateTime end,
            Pageable pageable
    );

    Optional<Payroll> findByReferenceTypeAndReferenceIdAndUserId(
            ReferenceType referenceType,
            UUID referenceId,
            UUID userId
    );
}