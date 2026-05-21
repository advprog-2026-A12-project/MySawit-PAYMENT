package id.ac.ui.cs.advprog.mysawitpayment.repository;

import id.ac.ui.cs.advprog.mysawitpayment.model.Payroll;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.ReferenceType;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface PayrollRepository extends JpaRepository<Payroll, UUID>, JpaSpecificationExecutor<Payroll> {

    Optional<Payroll> findByReferenceTypeAndReferenceIdAndUserId(
            ReferenceType referenceType,
            UUID referenceId,
            UUID userId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select payroll from Payroll payroll where payroll.id = :id")
    Optional<Payroll> findByIdForUpdate(@Param("id") UUID id);

    @Modifying
    @Query(value = """
            INSERT INTO payrolls (
                id, user_id, user_role, amount, kilogram, rate_per_kg, multiplier,
                status, description, reference_type, reference_id
            )
            VALUES (
                :id, :userId, :userRole, :amount, :kilogram, :ratePerKg, :multiplier,
                :status, :description, :referenceType, :referenceId
            )
            ON CONFLICT (reference_type, reference_id, user_id) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(
            @Param("id") UUID id,
            @Param("userId") UUID userId,
            @Param("userRole") String userRole,
            @Param("amount") BigDecimal amount,
            @Param("kilogram") BigDecimal kilogram,
            @Param("ratePerKg") BigDecimal ratePerKg,
            @Param("multiplier") BigDecimal multiplier,
            @Param("status") String status,
            @Param("description") String description,
            @Param("referenceType") String referenceType,
            @Param("referenceId") UUID referenceId
    );
}
