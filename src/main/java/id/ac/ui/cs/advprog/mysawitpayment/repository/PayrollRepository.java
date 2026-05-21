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
                :#{#payroll.id}, :#{#payroll.userId}, :#{#payroll.userRole.name()},
                :#{#payroll.amount}, :#{#payroll.kilogram}, :#{#payroll.ratePerKg},
                :#{#payroll.multiplier}, :#{#payroll.status.name()}, :#{#payroll.description},
                :#{#payroll.referenceType.name()}, :#{#payroll.referenceId}
            )
            ON CONFLICT (reference_type, reference_id, user_id) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(@Param("payroll") Payroll payroll);
}
