package id.ac.ui.cs.advprog.mysawitpayment.repository;

import id.ac.ui.cs.advprog.mysawitpayment.model.PaymentTransaction;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.PaymentTransactionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PaymentTransactionRepository
        extends JpaRepository<PaymentTransaction, UUID>, JpaSpecificationExecutor<PaymentTransaction> {

    Optional<PaymentTransaction> findByGatewayReferenceId(String gatewayReferenceId);

    Page<PaymentTransaction> findByAdminIdOrderByCreatedAtDesc(UUID adminId, Pageable pageable);

    Page<PaymentTransaction> findByAdminIdAndStatusOrderByCreatedAtDesc(
            UUID adminId,
            PaymentTransactionStatus status,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select tx from PaymentTransaction tx where tx.id = :id")
    Optional<PaymentTransaction> findByIdForUpdate(@Param("id") UUID id);
}
