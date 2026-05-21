package id.ac.ui.cs.advprog.mysawitpayment.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WageConfigTest {

    private WageConfig wageConfig;

    @BeforeEach
    void setUp() {
        wageConfig = WageConfig.builder()
                .upahBuruhPerKg(new BigDecimal("2000.00"))
                .upahSupirPerKg(new BigDecimal("2500.00"))
                .upahMandorPerKg(new BigDecimal("3000.00"))
                .updatedBy(UUID.randomUUID())
                .build();
    }

    @Test
    void testDefaultIsActive() {
        WageConfig config = WageConfig.builder()
                .upahBuruhPerKg(new BigDecimal("2000.00"))
                .upahSupirPerKg(new BigDecimal("2500.00"))
                .upahMandorPerKg(new BigDecimal("3000.00"))
                .updatedBy(UUID.randomUUID())
                .build();

        assertTrue(config.getIsActive());
    }

    @Test
    void testPrePersistSetsTimestamps() {
        wageConfig.prePersist();

        assertNotNull(wageConfig.getCreatedAt());
        assertNotNull(wageConfig.getEffectiveFrom());
    }

    @Test
    void testPrePersistSetsDefaultIsActiveWhenNull() {
        WageConfig config = WageConfig.builder()
                .upahBuruhPerKg(new BigDecimal("2000.00"))
                .upahSupirPerKg(new BigDecimal("2500.00"))
                .upahMandorPerKg(new BigDecimal("3000.00"))
                .updatedBy(UUID.randomUUID())
                .isActive(null)
                .build();

        config.prePersist();

        assertNotNull(config.getIsActive());
        assertTrue(config.getIsActive());
    }

    @Test
    void testPrePersistDoesNotOverrideEffectiveFrom() {
        OffsetDateTime customEffectiveFrom = OffsetDateTime.now(ZoneOffset.UTC).minusDays(1);

        WageConfig config = WageConfig.builder()
                .upahBuruhPerKg(new BigDecimal("2000.00"))
                .upahSupirPerKg(new BigDecimal("2500.00"))
                .upahMandorPerKg(new BigDecimal("3000.00"))
                .updatedBy(UUID.randomUUID())
                .effectiveFrom(customEffectiveFrom)
                .build();

        config.prePersist();

        assertEquals(customEffectiveFrom, config.getEffectiveFrom());
    }

    @Test
    void testDeactivate() {
        wageConfig.prePersist();

        wageConfig.deactivate();

        assertFalse(wageConfig.getIsActive());
    }

    @Test
    void testIsCurrentlyActiveTrue() {
        WageConfig config = WageConfig.builder()
                .upahBuruhPerKg(new BigDecimal("2000.00"))
                .upahSupirPerKg(new BigDecimal("2500.00"))
                .upahMandorPerKg(new BigDecimal("3000.00"))
                .updatedBy(UUID.randomUUID())
                .isActive(true)
                .build();

        assertTrue(config.isCurrentlyActive());
    }

    @Test
    void testIsCurrentlyActiveFalse() {
        WageConfig config = WageConfig.builder()
                .upahBuruhPerKg(new BigDecimal("2000.00"))
                .upahSupirPerKg(new BigDecimal("2500.00"))
                .upahMandorPerKg(new BigDecimal("3000.00"))
                .updatedBy(UUID.randomUUID())
                .isActive(false)
                .build();

        assertFalse(config.isCurrentlyActive());
    }

    @Test
    void testBuilderPattern() {
        UUID adminId = UUID.randomUUID();
        BigDecimal buruhRate = new BigDecimal("2200.00");
        BigDecimal supirRate = new BigDecimal("2700.00");
        BigDecimal mandorRate = new BigDecimal("3200.00");

        WageConfig config = WageConfig.builder()
                .upahBuruhPerKg(buruhRate)
                .upahSupirPerKg(supirRate)
                .upahMandorPerKg(mandorRate)
                .updatedBy(adminId)
                .isActive(false)
                .build();

        assertEquals(buruhRate, config.getUpahBuruhPerKg());
        assertEquals(supirRate, config.getUpahSupirPerKg());
        assertEquals(mandorRate, config.getUpahMandorPerKg());
        assertEquals(adminId, config.getUpdatedBy());
        assertFalse(config.getIsActive());
    }

    @Test
    void testImmutablePattern() {
        WageConfig oldConfig = WageConfig.builder()
                .upahBuruhPerKg(new BigDecimal("2000.00"))
                .upahSupirPerKg(new BigDecimal("2500.00"))
                .upahMandorPerKg(new BigDecimal("3000.00"))
                .updatedBy(UUID.randomUUID())
                .isActive(true)
                .build();
        oldConfig.prePersist();
        oldConfig.deactivate();

        WageConfig newConfig = WageConfig.builder()
                .upahBuruhPerKg(new BigDecimal("2200.00"))
                .upahSupirPerKg(new BigDecimal("2700.00"))
                .upahMandorPerKg(new BigDecimal("3200.00"))
                .updatedBy(UUID.randomUUID())
                .isActive(true)
                .build();
        newConfig.prePersist();

        assertFalse(oldConfig.isCurrentlyActive());
        assertTrue(newConfig.isCurrentlyActive());
    }
}