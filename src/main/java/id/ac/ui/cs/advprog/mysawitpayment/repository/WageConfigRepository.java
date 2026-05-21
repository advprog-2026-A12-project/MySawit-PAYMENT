package id.ac.ui.cs.advprog.mysawitpayment.repository;

import id.ac.ui.cs.advprog.mysawitpayment.model.WageConfig;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface WageConfigRepository extends JpaRepository<WageConfig, UUID> {

    Optional<WageConfig> findByIsActiveTrue();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from WageConfig w where w.isActive = true")
    Optional<WageConfig> findActiveForUpdate();

    Page<WageConfig> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
