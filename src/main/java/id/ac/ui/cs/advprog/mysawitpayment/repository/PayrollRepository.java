package id.ac.ui.cs.advprog.mysawitpayment.repository;

import id.ac.ui.cs.advprog.mysawitpayment.model.Payroll;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.PayrollStatus;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.ReferenceType;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PayrollRepository extends JpaRepository<Payroll, UUID> {

    List<Payroll> findByUserId(UUID userId);

    List<Payroll> findByUserIdAndStatus(UUID userId, PayrollStatus status);

    List<Payroll> findByUserIdAndCreatedAtBetween(
            UUID userId,
            OffsetDateTime startDate,
            OffsetDateTime endDate
    );

    Optional<Payroll> findByReferenceTypeAndReferenceIdAndUserId(
            ReferenceType referenceType,
            UUID referenceId,
            UUID userId
    );

}