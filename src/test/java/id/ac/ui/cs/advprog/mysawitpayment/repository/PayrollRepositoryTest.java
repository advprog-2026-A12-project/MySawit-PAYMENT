package id.ac.ui.cs.advprog.mysawitpayment.repository;

import id.ac.ui.cs.advprog.mysawitpayment.model.Payroll;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.PayrollStatus;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.ReferenceType;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.UserRole;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class PayrollRepositoryTest {

    @Autowired
    private PayrollRepository payrollRepository;

    private Payroll createPayroll(UUID userId) {
        return Payroll.builder()
                .userId(userId)
                .userRole(UserRole.BURUH)
                .amount(new BigDecimal("100000"))
                .kilogram(new BigDecimal("100"))
                .ratePerKg(new BigDecimal("1000"))
                .referenceType(ReferenceType.HARVEST)
                .referenceId(UUID.randomUUID())
                .description("Test payroll")
                .build();
    }

    @Test
    void testFindByUserId() {
        UUID userId = UUID.randomUUID();

        Payroll payroll = createPayroll(userId);
        payrollRepository.save(payroll);

        Page<Payroll> result = payrollRepository.findByUserId(
                userId,
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void testFindByUserIdAndStatus() {
        UUID userId = UUID.randomUUID();

        Payroll payroll = createPayroll(userId);
        payroll.setStatus(PayrollStatus.PENDING);

        payrollRepository.save(payroll);

        Page<Payroll> result = payrollRepository.findByUserIdAndStatus(
                userId,
                PayrollStatus.PENDING,
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void testFindByUserIdAndCreatedAtBetween() {
        UUID userId = UUID.randomUUID();

        Payroll payroll = createPayroll(userId);
        payrollRepository.save(payroll);

        OffsetDateTime start = OffsetDateTime.now().minusDays(1);
        OffsetDateTime end = OffsetDateTime.now().plusDays(1);

        Page<Payroll> result = payrollRepository.findByUserIdAndCreatedAtBetween(
                userId,
                start,
                end,
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void testFindByReferenceTypeAndReferenceIdAndUserId() {
        UUID userId = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();

        Payroll payroll = Payroll.builder()
                .userId(userId)
                .userRole(UserRole.BURUH)
                .amount(new BigDecimal("100000"))
                .kilogram(new BigDecimal("100"))
                .ratePerKg(new BigDecimal("1000"))
                .referenceType(ReferenceType.HARVEST)
                .referenceId(referenceId)
                .description("Test payroll")
                .build();

        payrollRepository.save(payroll);

        var result = payrollRepository
                .findByReferenceTypeAndReferenceIdAndUserId(
                        ReferenceType.HARVEST,
                        referenceId,
                        userId
                );

        assertThat(result).isPresent();
    }
}