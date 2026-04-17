package id.ac.ui.cs.advprog.mysawitpayment.repository;

import id.ac.ui.cs.advprog.mysawitpayment.model.WageConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WageConfigRepository extends JpaRepository<WageConfig, UUID> {

    Optional<WageConfig> findByIsActiveTrue();

    Page<WageConfig> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
