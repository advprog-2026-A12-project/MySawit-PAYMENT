package id.ac.ui.cs.advprog.mysawitpayment.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WageConfigTest {

    private WageConfig wageConfig;

    @BeforeEach
    void setUp() {
        UUID adminId = UUID.randomUUID();

        wageConfig = WageConfig.builder()
                .upahBuruhPerKg(new BigDecimal("2000.00"))
                .upahSupirPerKg(new BigDecimal("2500.00"))
                .upahMandorPerKg(new BigDecimal("3000.00"))
                .updatedBy(adminId)
                .build();
    }

    @Test
    void testDefaultIsActive() {
        WageConfig newConfig = WageConfig.builder()
                .upahBuruhPerKg(new BigDecimal("2000.00"))
                .upahSupirPerKg(new BigDecimal("2500.00"))
                .upahMandorPerKg(new BigDecimal("3000.00"))
                .updatedBy(UUID.randomUUID())
                .build();

        assertEquals(true, newConfig.getIsActive());
    }

    @Test
    void testPrePersistSetsTimestamps() {
        wageConfig.prePersist();

        assertNotNull(wageConfig.getCreatedAt());
        assertNotNull(wageConfig.getEffectiveFrom());
    }

    @Test
    void testPrePersistSetsDefaultIsActive() {
        WageConfig newConfig = new WageConfig();
        newConfig.setUpahBuruhPerKg(new BigDecimal("2000.00"));
        newConfig.setUpahSupirPerKg(new BigDecimal("2500.00"));
        newConfig.setUpahMandorPerKg(new BigDecimal("3000.00"));
        newConfig.setUpdatedBy(UUID.randomUUID());
        newConfig.prePersist();

        assertNotNull(newConfig.getIsActive());
        assertTrue(newConfig.getIsActive());
    }

    @Test
    void testPrePersistDoesNotOverrideEffectiveFrom() {
        OffsetDateTime customEffectiveFrom = OffsetDateTime.now().minusDays(1);
        wageConfig.setEffectiveFrom(customEffectiveFrom);
        wageConfig.prePersist();

        assertEquals(customEffectiveFrom, wageConfig.getEffectiveFrom());
    }

    @Test
    void testDeactivate() {
        wageConfig.prePersist();

        wageConfig.deactivate();

        assertFalse(wageConfig.getIsActive());
    }

    @Test
    void testIsCurrentlyActiveTrue() {
        wageConfig.setIsActive(true);

        assertTrue(wageConfig.isCurrentlyActive());
    }

    @Test
    void testIsCurrentlyActiveFalse() {
        wageConfig.setIsActive(false);

        assertFalse(wageConfig.isCurrentlyActive());
    }

    @Test
    void testGettersAndSetters() {
        UUID adminId = UUID.randomUUID();
        BigDecimal buruhRate = new BigDecimal("3000.00");
        BigDecimal supirRate = new BigDecimal("3500.00");
        BigDecimal mandorRate = new BigDecimal("4000.00");

        wageConfig.setUpahBuruhPerKg(buruhRate);
        wageConfig.setUpahSupirPerKg(supirRate);
        wageConfig.setUpahMandorPerKg(mandorRate);
        wageConfig.setUpdatedBy(adminId);
        wageConfig.setIsActive(false);

        assertEquals(buruhRate, wageConfig.getUpahBuruhPerKg());
        assertEquals(supirRate, wageConfig.getUpahSupirPerKg());
        assertEquals(mandorRate, wageConfig.getUpahMandorPerKg());
        assertEquals(adminId, wageConfig.getUpdatedBy());
        assertFalse(wageConfig.getIsActive());
    }

    @Test
    void testBuilderPattern() {
        UUID adminId = UUID.randomUUID();
        BigDecimal buruhRate = new BigDecimal("2200.00");
        BigDecimal supirRate = new BigDecimal("2700.00");
        BigDecimal mandorRate = new BigDecimal("3200.00");

        WageConfig newConfig = WageConfig.builder()
                .upahBuruhPerKg(buruhRate)
                .upahSupirPerKg(supirRate)
                .upahMandorPerKg(mandorRate)
                .updatedBy(adminId)
                .isActive(false)
                .build();

        assertEquals(buruhRate, newConfig.getUpahBuruhPerKg());
        assertEquals(supirRate, newConfig.getUpahSupirPerKg());
        assertEquals(mandorRate, newConfig.getUpahMandorPerKg());
        assertEquals(adminId, newConfig.getUpdatedBy());
        assertFalse(newConfig.getIsActive());
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

    @Test
    void testPrePersistHandlesNullStatus() {
        WageConfig wageConfig = new WageConfig();
        wageConfig.setIsActive(null);
        wageConfig.prePersist();

        assertNotNull(wageConfig.getIsActive());
        assertTrue(wageConfig.isCurrentlyActive());
    }
}